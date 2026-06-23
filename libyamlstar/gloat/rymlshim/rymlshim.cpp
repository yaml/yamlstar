#include "rymlshim.h"

#include <stdlib.h>
#include <string.h>

#include <exception>
#include <stdexcept>
#include <string>
#include <vector>

#include <c4/yml/parse_engine.def.hpp>
#include <c4/yml/extra/event_handler_ints.hpp>

// Build the minimal rapidyaml integer-event library in this package.
#include "../../../rapidyaml/src/c4/yml/common.cpp"
#include "../../../rapidyaml/src/c4/yml/node_type.cpp"
#include "../../../rapidyaml/src/c4/yml/tag.cpp"
#include "../../../rapidyaml/src_extra/c4/yml/extra/event_handler_ints.cpp"
#include "../../../rapidyaml/ext/c4core.src/c4/error.cpp"
#include "../../../rapidyaml/ext/c4core.src/c4/language.cpp"
#include "../../../rapidyaml/ext/c4core.src/c4/utf.cpp"

namespace {

char *copy_bytes(const char *src, size_t len)
{
    if(len == 0)
        return nullptr;
    char *dst = static_cast<char*>(malloc(len));
    if(!dst)
        throw std::bad_alloc();
    memcpy(dst, src, len);
    return dst;
}

char *copy_cstr(const std::string &s)
{
    char *dst = static_cast<char*>(malloc(s.size() + 1));
    if(!dst)
        return nullptr;
    memcpy(dst, s.data(), s.size());
    dst[s.size()] = '\0';
    return dst;
}

c4::substr to_substr(std::string &s)
{
    return c4::substr(s.empty() ? nullptr : &s[0], s.size());
}

std::string to_string(c4::csubstr s)
{
    return std::string(s.str ? s.str : "", s.len);
}

[[noreturn]] void throw_basic_error(c4::csubstr msg, c4::yml::ErrorDataBasic const&, void*)
{
    throw std::runtime_error(to_string(msg));
}

[[noreturn]] void throw_parse_error(c4::csubstr msg, c4::yml::ErrorDataParse const&, void*)
{
    throw std::runtime_error(to_string(msg));
}

[[noreturn]] void throw_visit_error(c4::csubstr msg, c4::yml::ErrorDataVisit const&, void*)
{
    throw std::runtime_error(to_string(msg));
}

RymlParseResult error_result(const std::string &message)
{
    RymlParseResult result = {};
    result.ok = 0;
    result.error = copy_cstr(message);
    return result;
}

void parse_once(
    const std::string &original,
    std::string *parsed,
    std::vector<int32_t> *events,
    std::vector<char> *arena,
    c4::yml::extra::EventHandlerInts *handler)
{
    *parsed = original;
    c4::substr src = to_substr(*parsed);
    c4::substr arena_substr(arena->empty() ? nullptr : arena->data(), arena->size());
    handler->reset(src, arena_substr, events->data(), static_cast<int32_t>(events->size()));
    c4::yml::ParseEngine<c4::yml::extra::EventHandlerInts> parser(handler);
    parser.parse_in_place_ev("yamlstar", src);
}

} // namespace

extern "C" RymlParseResult yamlstar_ryml_parse_events(const char *src, size_t src_len)
{
    try
    {
        std::string original(src ? src : "", src_len);
        c4::csubstr csrc(original.empty() ? nullptr : original.data(), original.size());
        int32_t estimated = c4::yml::extra::estimate_events_ints_size(csrc);
        if(estimated < 8)
            estimated = 8;

        std::vector<int32_t> events(static_cast<size_t>(estimated));
        std::vector<char> arena(original.size());
        std::string parsed;
        c4::yml::Callbacks callbacks = c4::yml::get_callbacks();
        callbacks.set_error_basic(throw_basic_error);
        callbacks.set_error_parse(throw_parse_error);
        callbacks.set_error_visit(throw_visit_error);
        c4::yml::extra::EventHandlerInts handler(callbacks);

        parse_once(original, &parsed, &events, &arena, &handler);

        if(!handler.fits_buffers())
        {
            size_t required_events = static_cast<size_t>(handler.required_size_events());
            size_t required_arena = handler.required_size_arena();
            if(required_events > events.size())
                events.resize(required_events);
            if(required_arena > arena.size())
                arena.resize(required_arena);

            parse_once(original, &parsed, &events, &arena, &handler);
            if(!handler.fits_buffers())
                return error_result("rapidyaml event or arena buffers did not fit after retry");
        }

        size_t required_events = static_cast<size_t>(handler.required_size_events());
        size_t required_arena = handler.required_size_arena();

        RymlParseResult result = {};
        result.ok = 1;
        result.events_len = required_events;
        result.source_len = parsed.size();
        result.arena_len = required_arena;

        if(required_events > 0)
        {
            result.events = static_cast<int32_t*>(malloc(required_events * sizeof(int32_t)));
            if(!result.events)
                throw std::bad_alloc();
            memcpy(result.events, events.data(), required_events * sizeof(int32_t));
        }
        result.source = copy_bytes(parsed.data(), parsed.size());
        result.arena = copy_bytes(arena.data(), required_arena);
        return result;
    }
    catch(const std::exception &e)
    {
        return error_result(e.what());
    }
    catch(...)
    {
        return error_result("unknown rapidyaml parse error");
    }
}

extern "C" void yamlstar_ryml_free_result(RymlParseResult *result)
{
    if(!result)
        return;
    free(result->error);
    free(result->events);
    free(result->source);
    free(result->arena);
    result->error = nullptr;
    result->events = nullptr;
    result->source = nullptr;
    result->arena = nullptr;
    result->events_len = 0;
    result->source_len = 0;
    result->arena_len = 0;
}

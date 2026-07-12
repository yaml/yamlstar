unit class YAMLStar;

use NativeCall;

constant YAMLSTAR_VERSION = v0.1.14;

sub resolve-lib {
  state $lib = do {
    my $ext = do given $*KERNEL.name {
      when 'darwin' { '.dylib' }
      when 'win32' { '.dll' }
      default { '.so' }
    }
    my @names = "libyamlstar$ext", "libyamlstar$ext" ~ "." ~ YAMLSTAR_VERSION.Str;
    my @paths = [
      $*PROGRAM-NAME.IO.dirname.IO.add('../../libyamlstar/lib').absolute,
      |(%*ENV<LD_LIBRARY_PATH>//'').split(':', :ignore-empty),
      '/usr/local/lib',
      %*ENV<HOME> ~ '/.local/lib',
    ].grep(* ne '');
    my $found;
    for @paths -> $path {
      for @names -> $name {
        my $file = $path.IO.add($name);
        if $file.e {
          $found = $file.absolute.Str;
          last;
        }
      }
      last if $found;
    }
    unless $found {
      $*ERR.say: qq:to/EOM/;
      Shared library file 'libyamlstar$ext' not found
      Try: curl -sSL https://yamlstar.org/install | LIB=1 bash
      EOM
      exit 1;
    }
    $found;
  }
  $lib;
}

sub load-json($json) { ::("Rakudo::Internals::JSON").from-json($json); }
sub dump-json($data) { ::("Rakudo::Internals::JSON").to-json($data); }

sub yamlstar_load(uint64, Str --> Str)
  is native(&resolve-lib) {*};

sub yamlstar_load_all(uint64, Str --> Str)
  is native(&resolve-lib) {*};

sub yamlstar_dump(uint64, Str --> Str)
  is native(&resolve-lib) {*};

sub yamlstar_dump_all(uint64, Str --> Str)
  is native(&resolve-lib) {*};

sub yamlstar_version(uint64 --> Str)
  is native(&resolve-lib) {*};

sub graal_create_isolate(uint64 is rw, uint64 is rw, uint64 is rw --> uint64)
  is native(&resolve-lib) {*};

sub graal_tear_down_isolate(uint64 --> uint64)
  is native(&resolve-lib) {*};

has uint64 $!isolate-thread;
has Bool $!closed = False;
has $.error is rw;

submethod BUILD {
  my uint64 ($n1, $n2);
  my $rc = graal_create_isolate($n1, $n2, $!isolate-thread);
  die "Failed to create GraalVM isolate"
    if $rc != 0;
}

submethod DESTROY {
  self.close unless $!closed;
}

method close {
  return if $!closed;
  my $rc = graal_tear_down_isolate($!isolate-thread);
  die "Failed to tear down GraalVM isolate"
    unless $rc == 0;
  $!closed = True;
}

method load(Str $input) {
  self!handle-response:
    yamlstar_load($!isolate-thread, $input);
}

method load-all(Str $input) {
  self!handle-response:
    yamlstar_load_all($!isolate-thread, $input);
}

method dump($value) {
  self!handle-response:
    yamlstar_dump($!isolate-thread, dump-json($value));
}

method dump-all($values) {
  self!handle-response:
    yamlstar_dump_all($!isolate-thread, dump-json($values));
}

method version {
  yamlstar_version($!isolate-thread);
}

method !handle-response(Str $json) {
  my %resp = load-json($json);
  $!error = %resp<error>;
  die "libyamlstar: %resp<error><cause>"
    if %resp<error>:exists;
  die "Unexpected response from 'libyamlstar'"
    unless %resp<data>:exists;
  %resp<data>;
}

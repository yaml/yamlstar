use ExtUtils::MakeMaker;

WriteMakefile(
    NAME             => 'YAMLStar',
    VERSION_FROM     => 'lib/YAMLStar.pm',
    ABSTRACT         => 'YAML 1.2 loader via FFI',
    AUTHOR           => 'Ingy döt Net <ingy@cpan.org>',
    LICENSE          => 'perl_5',
    MIN_PERL_VERSION => '5.16.0',

    PREREQ_PM => {
        'FFI::Platypus'      => '2.08',
        'FFI::CheckLib'      => '0.31',
        'Cpanel::JSON::XS'   => '4.37',
        'Moo'                => '2.0',
    },

    TEST_REQUIRES => {
        'Test2::V0' => '0.000159',
    },

    META_MERGE => {
        'meta-spec' => { version => 2 },
        resources => {
            repository => {
                type => 'git',
                url  => 'https://github.com/yaml/yamlstar.git',
                web  => 'https://github.com/yaml/yamlstar',
            },
        },
    },
);

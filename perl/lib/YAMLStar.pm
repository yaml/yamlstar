# Copyright 2024-2026 yaml.org
# This code is licensed under MIT license (See License for details)

use strict;
use warnings;

package YAMLStar;

use Moo;
use FFI::CheckLib ();
use FFI::Platypus;
use Cpanel::JSON::XS ();

our $VERSION = '0.1.3';

our $libyamlstar_version = $VERSION;

#------------------------------------------------------------------------------
# libyamlstar FFI setup:
#------------------------------------------------------------------------------

# Find the proper libyamlstar version:
my $libyamlstar = find_libyamlstar();

# Set up FFI functions:
my $ffi = FFI::Platypus->new(
    api => 2,
    lib => $libyamlstar,
);

my $graal_create_isolate = $ffi->function(
    graal_create_isolate =>
        ['opaque', 'opaque*', 'opaque*'] => 'int',
);

my $graal_tear_down_isolate = $ffi->function(
    graal_tear_down_isolate =>
        ['opaque'] => 'int',
);

my $yamlstar_load = $ffi->function(
    yamlstar_load =>
        ['sint64', 'string'] => 'string',
);

my $yamlstar_load_all = $ffi->function(
    yamlstar_load_all =>
        ['sint64', 'string'] => 'string',
);

my $yamlstar_version = $ffi->function(
    yamlstar_version =>
        ['sint64'] => 'string',
);

#------------------------------------------------------------------------------
# YAMLStar Moo attributes:
#------------------------------------------------------------------------------

has 'isolatethread' => (
    is => 'rw',
);

has 'error' => (
    is => 'rw',
);

#------------------------------------------------------------------------------
# YAMLStar object lifecycle:
#------------------------------------------------------------------------------

# BUILD is called after new() to create the graal isolate:
sub BUILD {
    my ($self, $args) = @_;

    my ($isolatethread);
    $graal_create_isolate->(undef, undef, \$isolatethread) == 0
        or die 'Failed to create graal isolate';

    $self->isolatethread(\$isolatethread);
}

# DEMOLISH tears down the graal isolate when the object goes out of scope:
sub DEMOLISH {
    my ($self) = @_;

    if (my $isolatethread = $self->isolatethread) {
        $graal_tear_down_isolate->($$isolatethread) == 0
            or die "Failed to tear down graal isolate";
    }
}

#------------------------------------------------------------------------------
# YAMLStar API methods:
#------------------------------------------------------------------------------

# Load a single YAML document:
sub load {
    my ($self, $yaml) = @_;

    $self->error(undef);

    my $resp = Cpanel::JSON::XS::decode_json(
        $yamlstar_load->(${$self->isolatethread}, $yaml)
    );

    return $resp->{data} if exists $resp->{data};

    if ($self->error($resp->{error})) {
        die "libyamlstar: " . $self->error->{cause};
    }

    die "Unexpected response from 'libyamlstar'";
}

# Load all YAML documents:
sub load_all {
    my ($self, $yaml) = @_;

    $self->error(undef);

    my $resp = Cpanel::JSON::XS::decode_json(
        $yamlstar_load_all->(${$self->isolatethread}, $yaml)
    );

    return $resp->{data} if exists $resp->{data};

    if ($self->error($resp->{error})) {
        die "libyamlstar: " . $self->error->{cause};
    }

    die "Unexpected response from 'libyamlstar'";
}

# Get the YAMLStar version:
sub version {
    my ($self) = @_;

    return $yamlstar_version->(${$self->isolatethread});
}

#------------------------------------------------------------------------------
# Helper functions:
#------------------------------------------------------------------------------

# Look for the local libyamlstar first, then look in system paths:
sub find_libyamlstar {
    my $vers = $libyamlstar_version;
    my $so = $^O eq 'darwin' ? 'dylib' : $^O eq 'MSWin32' ? 'dll' : 'so';
    my $name = "libyamlstar.$so.$vers";
    my @paths;

    # Add relative path for development:
    use File::Basename qw(dirname);
    use File::Spec;
    use Cwd qw(abs_path);

    my $module_file = abs_path(__FILE__);
    my $module_dir = dirname($module_file);
    # Go from perl/lib to libyamlstar/lib
    my $dev_path = File::Spec->catdir($module_dir, File::Spec->updir, File::Spec->updir, 'libyamlstar', 'lib');
    $dev_path = abs_path($dev_path) if -d $dev_path;
    push @paths, $dev_path if $dev_path && -d $dev_path;

    # Add LD_LIBRARY_PATH (Unix) or PATH (Windows):
    if ($^O eq 'MSWin32') {
        if (my $path = $ENV{PATH}) {
            push @paths, split /;/, $path;
        }
    } else {
        if (my $path = $ENV{LD_LIBRARY_PATH}) {
            push @paths, split /:/, $path;
        }
        # Add Unix system paths:
        push @paths, qw(
            /usr/local/lib
            /usr/local/lib64
            /usr/lib
            /usr/lib64
        ), "$ENV{HOME}/.local/lib";
    }

    for my $path (@paths) {
        if (-e "$path/$name") {
            return "$path/$name";
        }
    }

    die <<"..."
Shared library file $name not found
Search paths: @paths
Build with: cd libyamlstar && make native
...
}

1;

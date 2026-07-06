# Test image for the Haskell binding.
#
# Bakes GHC, cabal, the Hackage index and the compiled dependency
# tree of the yamlstar package into image layers, so running the tests
# never has to install a toolchain or compile dependencies.
#
# Rebuild and push when the dependencies in cabal.ys change:
#   make -C haskell docker-image-build docker-image-push

FROM ubuntu:latest

ARG GHC_VERSION=9.12.1
ARG CABAL_VERSION=3.14.2.0

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      ca-certificates curl xz-utils make gcc g++ binutils \
      binutils-gold libc6-dev libgmp-dev libtinfo6 libnuma1 \
      zlib1g-dev && \
    rm -rf /var/lib/apt/lists/*

# GHC bindist, minus profiling libraries and documentation:
RUN curl -sSL "https://downloads.haskell.org/ghc/$GHC_VERSION/ghc-$GHC_VERSION-x86_64-ubuntu22_04-linux.tar.xz" | \
      tar -xJ -C /opt && \
    mv /opt/ghc-$GHC_VERSION-* /opt/ghc && \
    find /opt/ghc \( -name '*_p.a' -o -name '*.p_hi' \) -delete && \
    rm -rf /opt/ghc/share/doc

RUN curl -sSL "https://downloads.haskell.org/~cabal/cabal-install-$CABAL_VERSION/cabal-install-$CABAL_VERSION-x86_64-linux-ubuntu22_04.tar.xz" | \
      tar -xJ -C /usr/local/bin cabal

ENV PATH=/opt/ghc/bin:$PATH
ENV CABAL_DIR=/opt/cabal

COPY yamlstar.cabal /deps/yamlstar.cabal

RUN mkdir -p /deps/lib /deps/test /tmp/libyamlstar/lib && \
    cd /deps && \
    cabal update && \
    cabal build --only-dependencies --enable-tests \
      --enable-benchmarks && \
    chmod -R a+rwX /opt/cabal /tmp/libyamlstar

DESCRIPTION = "nodeJS Evented I/O for V8 JavaScript"
HOMEPAGE = "http://nodejs.org"
LICENSE = "MIT & BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=914812f2875eef849b5c509cc25dcb4f"

DEPENDS = "openssl"

SRC_URI = "http://nodejs.org/dist/v${PV}/node-v${PV}.tar.gz \
           file://fix-hardfloat-detection.patch"
SRC_URI[md5sum] = "0da985a0bf820400af92363b9f453fe4"
SRC_URI[sha256sum] = "22265fd07e09c22f1d058156d548e7398c9740210f534e2f848eeab5b9772117"

S = "${WORKDIR}/node-v${PV}"

# v8 errors out if you have set CCACHE
CCACHE = ""

do_configure_virtclass-native () {
  sed -i -e s:\'/usr/lib:\'${STAGING_LIBDIR}:g wscript
  sed -i -e s:\'/usr/local/lib:\'${STAGING_LIBDIR}:g wscript

  ./configure --prefix=${prefix} --without-snapshot
}

# Node is way too cool to use proper autotools, so we install two wrappers to forcefully inject proper arch cflags to workaround waf+scons
do_configure () {
  echo '#!/bin/sh' > ${WORKDIR}/gcc
  echo '${CC} $@' >> ${WORKDIR}/gcc

  echo '#!/bin/sh' > ${WORKDIR}/g++
  echo '${CXX} $@'>> ${WORKDIR}/g++

  chmod +x ${WORKDIR}/gcc ${WORKDIR}/g++

  sed -i -e s:\'/usr/lib:\'${STAGING_LIBDIR}:g wscript
  sed -i -e s:\'/usr/local/lib:\'${STAGING_LIBDIR}:g wscript

  export PATH=${WORKDIR}:${PATH}
  export CC=gcc
  export CXX=g++

  ./configure --prefix=${prefix} --without-snapshot
}

do_compile_virtclass-native () {
  make BUILDTYPE=Release
}

do_compile () {
  export PATH=${WORKDIR}:${PATH}
  export CC=gcc
  export CXX=g++
  make BUILDTYPE=Release
}

do_install () {
  export DESTDIR=${D} 
  oe_runmake install
}

RDEPENDS_${PN} = "curl python-shell python-datetime python-subprocess python-crypt python-textutils python-netclient "
RDEPENDS_${PN}_virtclass-native = "curl-native python-native"

FILES_${PN} += "${libdir}/node/wafadmin ${libdir}/node_modules"
BBCLASSEXTEND = "native"

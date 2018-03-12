#! /bin/sh

set -e

test -f /usr/share/polkit-1/actions/org.eclipse.linuxtools.vagrant.policy || install -D -m 644 org.eclipse.linuxtools.vagrant.policy /usr/share/polkit-1/actions/org.eclipse.linuxtools.vagrant.policy

echo '#!/bin/sh' > vagrant || exit 1
echo 'exec pkexec /usr/bin/vagrant ${1+"$@"}' >> vagrant
chmod +x ./vagrant

echo Eclipse Vagrant plugin install successful.

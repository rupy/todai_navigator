#! /bin/sh

sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get -y install oracle-java8-installer
sudo apt-get -y install oracle-java8-set-default
java -version

#For android studio
sudo apt-get -y install libc6-i386 lib32stdc++6 lib32z1
sudo apt-get install android-tools-adb
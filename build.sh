#!/usr/bin/env bash
sudo add-apt-repository ppa:linuxuprising/java -y
sudo apt update
sudo apt install oracle-java15-installer maven -y
sudo apt install oracle-java15-set-default
sudo apt install maven -y

mvn package
# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/trusty64"
  config.vm.network "forwarded_port", host: 8153, guest: 8153
  config.vm.network "private_network", ip: '192.168.1.101'

  config.vm.provider "virtualbox" do |vb|
    vb.name = "go-server"
    vb.memory = 2048
  end

  config.vm.synced_folder "build/libs", "/var/lib/go-server/plugins/external"

  config.vm.provision "ansible" do |ansible|
    ansible.playbook = "go-server-playbook.yml"

    ansible.groups = {
      "go-server": ["default"]
    }
  end
end

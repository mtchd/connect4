provider "aws" {
  profile    = "Connect4"
  region     = "ap-southeast-2"
}

resource "aws_instance" "connect4" {
  ami           = "ami-0dc96254d5535925f"
  instance_type = "t2.micro"
  key_name = "connect4"
  vpc_security_group_ids = ["sg-013088ddfb67a3198"]
  subnet_id = "subnet-b5b7a5d2"

  provisioner "remote-exec" {

    connection {
      host = aws_instance.connect4.public_ip
      type = "ssh"
      user = "ec2-user"
      private_key = file("connect4.pem")
      timeout = "10m"
      agent = false
    }

    inline = [
      "sudo yum update -y",
      "sudo yum install docker -y",
      "sudo usermod -a -G docker ec2-user",
      "sudo setfacl -m user:ec2-user:rw unix:///var/run/docker.sock:",
      "sudo service docker start",
      "sudo docker pull mtchd/connect4",

      "docker run --rm -it -e ${TOKEN} connect4"
    ]
  }
}
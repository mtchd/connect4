resource "aws_instance" "connect4dev" {

  ami           = "ami-0dc96254d5535925f"
  instance_type = "t2.micro"
  key_name = "connect4"
  vpc_security_group_ids = ["sg-013088ddfb67a3198"]
  subnet_id = "subnet-b5b7a5d2"
  iam_instance_profile = "Connect4"

  tags = {
    Name = "connect4dev"
  }

  provisioner "file" {

    connection {
      host = aws_instance.connect4dev.public_ip
      type = "ssh"
      user = "ec2-user"
      private_key = file("connect4.pem")
      timeout = "10m"
      agent = false
    }

    source      = "encrypted/dev.encrypted"
    destination = "dev.encrypted"
  }

  provisioner "file" {

    connection {
      host = aws_instance.connect4dev.public_ip
      type = "ssh"
      user = "ec2-user"
      private_key = file("connect4.pem")
      timeout = "10m"
      agent = false
    }

    source      = "auto/remote"
    destination = "remote"
  }

  provisioner "remote-exec" {

    connection {
      host = aws_instance.connect4dev.public_ip
      type = "ssh"
      user = "ec2-user"
      private_key = file("connect4.pem")
      timeout = "10m"
      agent = false
    }

    inline = [
      "chmod +x remote",
      "./remote",
    ]

  }
}

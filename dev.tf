resource "aws_instance" "connect4dev" {

  ami           = "ami-0dc96254d5535925f"
  instance_type = "t3.micro"
  key_name = "connect4"
  vpc_security_group_ids = ["sg-013088ddfb67a3198"]
  subnet_id = "subnet-b5b7a5d2"
  iam_instance_profile = "Connect4"
  user_data = file("auto/remote")

  tags = {
    Name = "connect4dev"
  }

}

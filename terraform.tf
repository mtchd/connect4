provider "aws" {
  region     = "ap-southeast-2"
}

terraform {
  backend "s3" {
    bucket = "mtchd-connect4"
    key    = "terraform.tfstate"
    region = "ap-southeast-2"
    profile = "Connect4"
  }
}

variable "dbPassword" {
  type = string
}

resource "aws_instance" "connect4" {
  ami           = "ami-0dc96254d5535925f"
  instance_type = "t3.micro"
  key_name = "connect4"
  vpc_security_group_ids = ["sg-013088ddfb67a3198"]
  subnet_id = "subnet-b5b7a5d2"
  iam_instance_profile = "Connect4"
  user_data = file("auto/remote-prod")

  tags = {
    Name = "connect4"
  }

  lifecycle  {
    create_before_destroy = true
  }
}

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

  lifecycle  {
    create_before_destroy = true
  }
}

resource "aws_db_instance" "connect4" {
  allocated_storage    = 20
  storage_type         = "gp2"
  engine               = "postgres"
  engine_version       = "11.5"
  instance_class       = "db.t2.micro"
  name                 = "connect4"
  username             = "connect4"
  password             = var.dbPassword
  port                 = "5432"
  identifier           = "connect4"
  vpc_security_group_ids = ["sg-013088ddfb67a3198"]
  skip_final_snapshot  = true
  publicly_accessible  = true
}
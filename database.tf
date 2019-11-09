resource "aws_db_instance" "connect4" {
  allocated_storage    = 20
  storage_type         = "gp2"
  engine               = "postgreSQL"
  engine_version       = "11.5"
  instance_class       = "db.t3.micro"
  name                 = "connect4"
  username             = "connect4"
  password             = var.dbPassword
}

variable "dbPassword" {
  type = string
}
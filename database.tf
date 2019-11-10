resource "aws_db_instance" "connect4" {
  allocated_storage    = 20
  storage_type         = "gp2"
  engine               = "postgres"
  engine_version       = "11.5"
  instance_class       = "db.t3.micro"
  name                 = "connect4"
  username             = "connect4"
  password             = var.dbPassword
  port                 = "5432"
  identifier           = "connect4"
  vpc_security_group_ids = ["sg-013088ddfb67a3198"]
  skip_final_snapshot  = true
  publicly_accessible  = true
}

variable "dbPassword" {
  type = string
}
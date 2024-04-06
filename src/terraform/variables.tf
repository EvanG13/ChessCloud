variable "region" {
  default = "us-east-1"
}

variable "shared_config_files" {
  default = ["~/.aws/config"]
}

variable "shared_credentials_files" {
  default = ["~/.aws/credentials"]
}

variable "profile" {
  default = "default"
}

variable "lambda_runtime" {
  default = "java17"
}

variable "handlers_source_path" {
  default = "main/java/"
}

variable "handlers_output_path" {
  default = "target/zip"
}
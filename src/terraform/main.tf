terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6.0"
    }
  }

  backend "s3" {
    bucket = "tf-state-guinea-pig"
    key    = "terraform.tfstate"
    region = "us-east-1"
  }

  required_version = ">= 1.7.5"
}

provider "aws" {
  shared_config_files      = var.shared_config_files
  shared_credentials_files = var.shared_credentials_files
  region                   = var.region
  profile                  = var.profile
}

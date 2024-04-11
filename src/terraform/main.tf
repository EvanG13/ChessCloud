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

  required_version = "~> 1.7.5"
}

provider "aws" {
  shared_config_files      = var.shared_config_files
  shared_credentials_files = var.shared_credentials_files
  region                   = var.region
  profile                  = var.profile
}

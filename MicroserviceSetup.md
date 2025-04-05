

# 1. Environment Setup (Ubuntu Server)

This document walks you through setting up your development environment for running and deploying a microservices architecture using Minikube, Kubernetes, Docker, Maven, and Helm on **Ubuntu Server**.

----------

## ✅ Requirements

-   Ubuntu Server (20.04 or later recommended - refer to the installation tutorial in KVMSetup.md)
    
-   Internet connection
    
-   User with sudo privileges(Can use ```sudo su``` command as well)
    

----------

## 🧰 Step-by-Step Installation

### 1. Update System

```bash
sudo apt update && sudo apt upgrade -y

```

### 2. Install Docker

```bash
sudo apt install docker.io -y
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker $USER

```

### 3. Install Minikube

```bash
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

```

### 4. Install Kubectl

```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

```

### 5. Install Helm

```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

```

### 6. Install Java & Maven

```bash
sudo apt install openjdk-17-jdk maven -y

```

----------

## 🚀 Start Minikube

```bash
minikube start --driver=docker
```
**OR**

```
bash`minikube start --driver=virtualbox`
```

If you're using SSH into Ubuntu Server without GUI:

```bash
minikube start --driver=docker --no-vtx-check

```

----------

## 🧪 Verify Installations

```bash
# Docker
docker --version

# Minikube
minikube version

# Kubectl
kubectl version --client

# Helm
helm version

# Java
java -version

# Maven
mvn -v

```

#!/bin/sh

java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv confidence 0.70 50
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv dsa 0.707 50
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv lsa 30243.43 50
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv combo 0.70 50

java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv confidence 0.70 100
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv dsa 0.707 100
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv lsa 30243.43 100
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv combo 0.70 100

java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv confidence 0.70 200
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv dsa 0.707 200
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv lsa 30243.43 200
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv combo 0.70 200

java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv confidence 0.70 400
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv dsa 0.707 400
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv lsa 30243.43 400
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv combo 0.70 400

java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv confidence 0.70 800
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv dsa 0.707 800
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv lsa 30243.43 800
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv combo 0.70 800


java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv confidence 0.70 50
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv dsa 2.503 50
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv lsa 111.898 50
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv combo 0.70 50

java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv confidence 0.70 100
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv dsa 2.503 100
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv lsa 111.898 100
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv combo 0.70 100

java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv confidence 0.70 200
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv dsa 2.503 200
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv lsa 111.898 200
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv combo 0.70 200

java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv confidence 0.70 400
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv dsa 2.503 400
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv lsa 111.898 400
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv combo 0.70 400

java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv confidence 0.70 800
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv dsa 2.503 800
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv lsa 111.898 800
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv combo 0.70 800
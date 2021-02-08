#!/bin/sh

java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv confidence 0.70 200
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv dsa 0.707 200
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv lsa 30243.43 200
java -jar DeepEST.jar metrics_datasets/CN5_MNIST.csv combo 0.70 200

java -jar DeepEST.jar metrics_datasets/LN5_MNIST.csv confidence 0.70 200
java -jar DeepEST.jar metrics_datasets/LN5_MNIST.csv dsa 0.757 200
java -jar DeepEST.jar metrics_datasets/LN5_MNIST.csv lsa 1034.12 200
java -jar DeepEST.jar metrics_datasets/LN5_MNIST.csv combo 0.70 200

java -jar DeepEST.jar metrics_datasets/CN12_CIFAR10.csv confidence 0.70 200
java -jar DeepEST.jar metrics_datasets/CN12_CIFAR10.csv dsa 1.574 200
java -jar DeepEST.jar metrics_datasets/CN12_CIFAR10.csv lsa 460.2 200
java -jar DeepEST.jar metrics_datasets/CN12_CIFAR10.csv combo 0.70 200

java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR10.csv confidence 0.70 200
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR10.csv dsa 2.133 200
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR10.csv lsa 3052.397 200
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR10.csv combo 0.70 200

java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv confidence 0.70 200
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv dsa 2.503 200
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv lsa 111.898 200
java -jar DeepEST.jar metrics_datasets/VGG16_CIFAR100.csv combo 0.70 200
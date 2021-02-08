This repository is a companion page for a research paper submitted to the 43rd International Conference on Software Engineering (ICSE'21), "Operation is the hardest teacher: estimating DNN accuracy looking for mispredictions", which presents DeepEST: Deep  neural networks Enhanced Sampler for operational Testing" . It contains all the material required for replicating our experiments, including: the implementation of the algorithms and the input datasets. 

To reproduce the results obtained by DeepEST, use the "run.sh" script (results of RQ1). The "runSensitivity.sh" allows running the experiments under different sample size (results of RQ1). To replicate the experiments on different subjects, just modify the "run.sh" script. 
The DeepEST algorithm code is DeepEST.jar. To run it, you can use: "java -jar DeepEST.jar input1 input2 input3", where input1 is the auxiliary variable (either 'confidence', 'lsa'. 'dsa', or 'combo'), input2 is the threshold value associated with the auxiliary variable (see the paper for more details) input3 is the sample size (the number of examples to select). 

As for the competing approach CES, the code to run it is available at: https://github.com/Lizn-zn/DNNOpAcc

The code to compute DSA and LSA for a new dataset is available at: https://github.com/coinse/sadl

These links also contain the models used in the experimentation. The VGG16 DNN weights are available at: https://github.com/geifmany/cifar-vgg

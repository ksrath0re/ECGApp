package com.example.kuldeep.ecgapp.MachineLearning;

import android.util.Log;

import libsvm.*;

import java.util.*;

public class CustomSVM {

    List<double[]> training_data;
    List<double[]> testing_data;

    public CustomSVM(double[][] t_data, double[][] test_data) {
        training_data = new ArrayList<>();
        for (int i = 0; i < t_data.length; i++) {
            training_data.add(t_data[i]);
        }
        testing_data = new ArrayList<>();
        for (int i = 0; i < test_data.length; i++) {
            testing_data.add(test_data[i]);
        }
    }

    private svm_model svm_train() {
        svm_problem problem = new svm_problem();
        int input_length = training_data.size();
        problem.y = new double[input_length];
        problem.x = new svm_node[input_length][];
        problem.l = input_length;
        for (int i = 0; i < input_length; i++) {
            double[] feature_set = training_data.get(i);
            problem.x[i] = new svm_node[1];
            for (int k = 1; k < 2; k++) {
                svm_node temp = new svm_node();
                temp.index = k;
                temp.value = feature_set[k];
                problem.x[i][k - 1] = temp;
            }
            problem.y[i] = feature_set[0];
        }
        svm_parameter parameters = new svm_parameter();
        parameters.probability = 0;
        parameters.gamma = 0.5;
        parameters.nu = 0.5;
        parameters.C = 1;
        parameters.svm_type = svm_parameter.C_SVC;
        parameters.kernel_type = svm_parameter.LINEAR;
        parameters.cache_size = 100;
        parameters.eps = 0.01;
        svm_model svmModel = svm.svm_train(problem, parameters);
        return svmModel;
    }

    private double[] svm_predict(svm_model model) {
        double[] predicted = new double[testing_data.size()];
        for (int i = 0; i < testing_data.size(); i++) {
            double[] features = testing_data.get(i);
            svm_node[] n = new svm_node[1];
            for (int j = 1; j < 2; j++) {
                svm_node node = new svm_node();
                node.index = j;
                node.value = features[j];
                n[j - 1] = node;
            }
            int[] labels = new int[2];
            svm.svm_get_labels(model, labels);
            double[] prob = new double[2];
            predicted[i] = svm.svm_predict_probability(model, n, prob);
        }
        return predicted;
    }

    public void evaluate() {
        svm_model model = svm_train();
        double[] predicted = svm_predict(model);
        int count = 0;
        for (int i = 0; i < testing_data.size(); i++) {
            if(testing_data.get(i)[0] != predicted[i]) {
                count++;
            }
        }
        System.out.println("missclassified samples = " + count);
    }
}

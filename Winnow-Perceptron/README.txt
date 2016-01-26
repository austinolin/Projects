In order to run:

GenerateLMN:

	Params: int l, int m, int n, boolean noisy

	Example:
		java GenerateLMN 10 100 500 false > data_10_100_500.libsvm


Perceptron:
	Params: int positions(number of positions in an instance), String sourcefile, int margin (0 or 1), boolean halting(true for experiment 2, allows for the convergence halting rule to take place)

	Example:
		java Perceptron 500 data_10_100_500.libsvm 0 false

Winnow:
	Params: int positions(number of positions in an instance), String sourcefile, int margin (0 or 1), boolean halting(true for experiment 2, allows for the convergence halting rule to take place)

	Example:
		java Winnow 500 data_10_100_500.libsvm 0 false

BatchPerceptron (for problem 3):
	Params: int positions(number of positions in an instance), String sourcefile, String testfile, int margin (0 or 1), boolean halting(true for experiment 2, allows for the convergence halting rule to take place)

	Example:
		java BatchPerceptron 500 noisy_10_100_1000.libsvm test_10_100_1000.libsvm false


BatchWinnow (for problem 3):
	Params: int positions(number of positions in an instance), String sourcefile, String testfile, int margin (0 or 1), boolean halting(true for experiment 2, allows for the convergence halting rule to take place)

	Example:
		java BatchWinnow 500 noisy_10_100_1000.libsvm test_10_100_1000.libsvm false
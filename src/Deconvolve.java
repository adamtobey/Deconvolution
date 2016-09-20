public class Deconvolve {

	private DCTComputer dct;

	public Deconvolve(DCTComputer dct) {
		this.dct = dct;
	}

	/**
		deconvolves an image as a 3D int array, each index representing a color channel, that was convoluted with the given kernel

		@param image the convoluted image in [channel][x][y] format
		@param blocks the array matrices of quantization blocks used in the JPEG encoding of the convoluted image in [channel][block][x][y] format
		@param kernel the kernel used for the convolution in [x][y] format
		@return the deconvolved image in [channel][x][y] format
	*/
	public int[] deconvovle(int[][][] image, int[][][][] qblocks, double[][] kernel) {
		if (image.length != blocks.length) {
			throw new IllegalArgumentException("Image and block lengths must be the same");
		}
		int[] ret = new int[image.length];
		for (int channel = 0, len = image.length; channel < len; channel++) {
			ret[channel] = decon(image[channel], qblocks[channel], kernel);
		}
		return ret;


	/**
		deconvolves an image channel as a SimpleMatrix that convoluted with the given kernel

		@param image the convoluted single-channel image in [x][y] format
		@param blocks the array matrix of quantization blocks used in the JPEG encoding of the convoluted image
		@param kernel the kernel used for the convolution in [x][y] format
		@return the deconvolved channel in [x][y] format
	*/
	private int[][] decon(int[][] image, int[][][] qblocks, double[][] kernel) {
		//// calculate naive solution using linear system
		// generate coefficient matrix from kernel
		if (image.length <= 0) throw new IllegalArgumentException("Image must be non-zero");
		if (kernel.length % 2 == 0 || kernel[0].length % 2 == 0) throw new IllegalArgumentException("Kernel dimensions must be odd");
		int count = image.length * image[0].length;
		int kwidth = kernel.length, kheight = kernel[0].length;
		int dkwidth = kwidth / 2, dkheight = kheight / 2; // purposeful integer division to get floor of odd div by 2
		int width = image.length, height = image[0].length;
		int x, y;
		double[][] coefficients = new double[count][count];
		// efficient convolution is not the point of the algorithm
		for (int cx = 0; cx < width; cx++) {
			for (int cy = 0; cy < height; cy++) {
				// TODO edge extension?
				for (int kx = 0; kx < kwidth; kx++) {
					x = cx - dkwidth;
					if (x < 0 || x > width) continue;
					for (int ky = 0; ky < kheight; ky++) {
						y = cy - dkwidth;
						if (y < 0 || y > height) continue;
						
						coeff[x * width + y][cx * width + cy] = kernel[kx][ky];
					}
				}
			}
		}

		// TODO swap indices
		double[][] imageColumnVector = new double[count][1];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				imageColumnVector[x * width + height][1] = (double) image[x][y];
			}
		}

		// solve as linear system
		LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.general(count, count);
		DenseMatrix64F output = new DenseMatrix64F(imageColumnVector);
		DenseMatrix64F transform = new DenseMatrix64F(coefficients);
		solver.setA(transform);

		DenseMatrix64F solution = new DenseMatrix64F(1, count);
		solver.solve(output, solution);
		
		// DCT solution
		double[][][] dct = DCTComputer.forward(solution);
		int blocks = dct.length;
		int dctWidth = dct[0].length;
		int dctHeight = dct[0][0].length;
		// project into error bounds of quantization table
		double[] norm = new double[blocks];
		for (int i = 0; i < blocks; i++) {
			// TODO make sure this is the base component
			norm[i] = dct[i][0][0];
		}
		
		double[][][] adjusted =	new double[blocks][dctWidth][dctHeight];
		for (int i = 0; i < blocks; i++) {
			adjusted[i] = projector.project(dct[i], qblocks[i]);
			normalize(adjusted[i], norm[i]/adjusted[i][0][0]);
		}
		// IDCT
		double[][] ret = DCTComputer.backward(adjusted);
		return ret;
		// return
	}

	private void normalize(double[][] matrix, double factor) {
		for (int x = 0, width = matrix.length; x < width; x++) {
			for (int y = 0, height = matrix[0].length; y < height; y++) {
				matrix[x][y] = matrix[x][y]*factor;
			}
		}
	}

}

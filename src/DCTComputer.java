public interface DCTComputer {

	/**
		Compute the forward 8x8 DCT on the given image
		
		@param image the image to transform as a DenseMatrix64F
		@return the DCT blocks in [block][x][y] format, with block indices
			corresponding to the order of the quantization table blocks in
			JPEG implementation
	*/
	public double[][][] forward(DenseMatrix64F image);

	/**
		Compute the (inverse) 8x8 IDCT on the given DCT blocks
		
		@param blocks the DCT blocks to transform in [block][x][y] format
			corresponding to the return value of the 'forward' function above
		@return the inverse transformed image in [x][y] format
	*/
	public double[][] backward(double[][][] blocks);

}

public class EllipsoidProjector64 {

	public double[][] project(double[][] matrix, int[][] errorBounds) {
		if (matrix.length != 8 || matrix[0].length != 8)
			throw new IllegalArgumentException("Matrix must be 8x8");
		if (errorBounds.length != 8 || errorBounds[0].length != 8)
			throw new IllegalArgumentException("Error bounds must be 8x8");
		double[] quant = new double[64];
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				quant[x * 8 + height] = errorBounds[x][y];
			}
		}
		
		double[] point = new double[64];
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				point[x * 8 + height] = matrix[x][y];
			}
		}
		double[] radii = new double[64];
		for (int i = 0; i < 64; i++) {
			radii[i] = point[i] - Math.round(quant[i] / 2);
		}

		double[] guess = surfaceParam(radii, normalize(point));
		double err, accept = 0.005; //TODO magic number
		do {
			int pnormb = 0;
			for (int b = 0; b < 64; b++) {
				if (guess[b] > point[pnormb]) {
					pnormb = b;
				}
			}
			double gaussian = 1;
			double[] normal = new double[64];
			normal[pnormb] = 1;
			for (int b = 0; b < 64; b++) {
				if (b == pnormb) continue;
				gaussian *= curvature(radii, guess, pnormb, b);
				normal[b] = norm(radii, guess, pnormb, b);
			}
			normalize(normal);
			double[] center = vecSub(guess, vecScale(gaussian, normal));
			double[] tguess = sphereProject(center, gaussian, point);
			guess = surfaceParam(radii, normalize(tguess));
			err = vecErr(normal, normalize(vecSub(guess, point)));
		} while (err > accept);
		return guess;
	}

	private double vecMag(double[] vec) {
		double mag = 0;
		foreach (double c : vec) {
			mag += c*c;
		}
		return Math.sqrt(mag);
	}

	private double vecErr(double[] a, double[] b) {
		// lp1 norm
		double err = 0;
		int count = a.length;
		for (int i = 0; i < count; i++) {
			err += a[i] - b[i];
		}
		return err;
	}

	private double[] vecAdd(double[] a, double[] b) {
		int count = a.length;
		double[] ret = new double[count];
		for (int i = 0; i < count; i++) {
			ret[i] = a[i] + b[i];
		}
		return ret;
	}

	private double[] vecSub(double[] a, double[] b) {
		int count = a.length;
		double[] ret = new double[count];
		for (int i = 0; i < count; i++) {
			ret[i] = a[i] - b[i];
		}
		return ret;
	}

	private double[] vecScale(double scale, double[] vec) {
		int count = a.length;
		double[] ret = new double[count];
		for (int i = 0; i < count; i++) {
			ret[i] = scale * vec[i];
		}
		return ret;
	}

	private sphereProject(double[] center, double radius, double[] point) {
		double[] norm = normalize(vecSub(point, center));
		double[] point = vecAdd(center, vecScale(radius, norm));
		return point;
	}

	/**
		Calculates the point on the surface of a spheroid of given radii whose
			normalized value is given norm. This is a parameterization of the 
			surface in terms of the unit vector from the center

		@param radii radii of the ellipsoid
		@param norm vector representing the normal from the center
		@return the point on the surface that is oriented with given norm
	*/
	private double[] surfaceParam(double[] radii, double[] norm) {
		double num = 1;
		foreach (double r : radii) {
			num *= r;
		}
		double num2 = num*num;
		
		double denom = 1;
		double r, n;
		for (int i = 0; i < 64; i++) {
			r = radii[i];
			n = norm[i];
			denom += num2 / (r * r) * n * n;
		}
		denom = Math.sqrt(denom);

		return vecScale(num/denom, norm);
	}

	private double[] normalize(double[] vec) {
		double factor = vecMag(vec);
		return vecScale(1/factor, vec);
	}

	/**
		Calculate the normal vector of the ellipse defined as the intersection
			of the ellipsoid of given radii and the span of the standard basis 
			vectors of index b1 and b2
	
		@param radii the vector of radii of the ellipsoid as double[] in order
			of dimension
		@param point the point on the surface of the ellipsoid at which to give
			the normal
		@param b1 the standard basis index of the main vector
		@param b2 the standard basis index of the second vector
		@return the normal vector
	*/
	private double[] norm(double[] radii, double[] point, int b1, int b2) {
		//TODO
		double a = radii[b1];
		double b = radii[b2];
		double n = point[b1];
		double m = point[b2];
		double num = a * n;
		double k = 0;
		double p, r;
		for (int i = 0; i < 64; i++) {
			if (i == b1 || i == b2) continue;
			p = point[i];
			r = radii[i];
			k += p * p / (r * r);
		}
		double denom = a * a * Math.sqrt(1 - k - (n * n) / (a * a));
		return -num / denom;
	}

	/**
		Calculate the curvature of the ellipsoid of given radii on the
			plane defined as the span of the standard basis vectors of
			index b1 and b2, at the given point
	
		@param radii the vector of radii of the ellipsoid as double[] in order
			of dimension
		@param point the point on the surface of the ellipsoid at which to give
			the curvature
		@param b1 the standard basis index of the first vector
		@param b2 the standard basis index of the second vector
		@return the surface curvature of the ellipse defined as the intersection
			of the specified ellipsoid and the span of the standard basis vectors
			of index b1 and b2
	*/
	private double curvature(double[] radii, double[] point, int b1, int b2) {
		double[] xy = new double[]{point[b1], point[b2]};
		double k = 0
		double p;
		double r;
		for (i = 0; i < 64; i++) {
			if (i == b1 || i == b2) continue;
			p = point[i]; 
			r = radii[i];
			k += p * p / (r * r);
		}
		double fact = Math.sqrt(1 - k);
		double a = radii[b1] / fact, b = radii[b2] / fact};
		double t = Math.atan(xy[1] / xy[0]);
		double num = a * b;
		double st = Math.sin(t), ct = Math.cos(t);
		double denom = Math.sqrt(a*a*st*st + b*b*ct*ct);
		denom = denom * denom * denom;
		return num / denom;
	}

}

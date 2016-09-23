# Error Reduction in Deconvolution Algorithms
*Adam Tobey*

### Abstract

Image filters are prevalent in the world of graphics processing and production. A great amount of research and development goes into creating these filters that make the basis for quick and easy image manipulation that we see every day. Many of these filters are theoretically invertible transformations and can therefore be reversed simply as the solution to a linear system experessing the convolution. However, this naive solution allows error to propagate into the output image. This research proposes a method of reducing the effect of error by weighting the visual contribution of information based on its estimated level of error.

## Introduction

### Definitions

**Convolution:** discrete convolution, the kind discussed in this project, is a transformation of one signal *A* by another *B* that maps each entry of the first signal *n* to the sum of the product of each entry around it (*n - k* for *k* up to the length of *B*) with the corresponding entry of *B* translated to center around *n*.



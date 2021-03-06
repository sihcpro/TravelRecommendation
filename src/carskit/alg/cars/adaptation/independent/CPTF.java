// Copyright (C) 2015 Yong Zheng
//
// This file is part of CARSKit.
//
// CARSKit is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// CARSKit is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with CARSKit. If not, see <http://www.gnu.org/licenses/>.
//

package carskit.alg.cars.adaptation.independent;

import carskit.generic.TensorRecommender;
import carskit.data.structure.SparseMatrix;
import librec.data.DenseMatrix;
import librec.data.TensorEntry;

/**
 * CANDECOMP/PARAFAC (CP) Tensor Factorization <br>
 *
 * Shao W., <strong>Tensor Completion</strong> (Section 3.2), Saarland University.
 *
 * Note: This implementation is modified from the algorithm in LibRec
 *
 * @author Yong Zheng
 *
 */
public class CPTF extends TensorRecommender {

    // dimension-feature matrices
    private DenseMatrix[] M;

    public CPTF(SparseMatrix trainMatrix, SparseMatrix testMatrix, int fold) throws Exception {
        super(trainMatrix, testMatrix, fold);
    }

    public CPTF() {
    	super();
	}

	@Override
    protected void initModel() throws Exception {
        M = new DenseMatrix[numDimensions];

        for (int d = 0; d < numDimensions; d++) {
            M[d] = new DenseMatrix(dimensions[d], numFactors);
            //M[d].init(initMean,initStd);
            //normalize(d);
            M[d].init(1, 0.1); // randomly initialization
        }
    }

    protected void normalize(int d) {

        // column-wise normalization
        for (int f = 0; f < numFactors; f++) {

            double norm = 0;
            for (int r = 0; r < M[d].numRows(); r++) {
                norm += Math.pow(M[d].get(r, f), 2);
            }
            norm = Math.sqrt(norm);

            for (int r = 0; r < M[d].numRows(); r++) {
                M[d].set(r, f, M[d].get(r, f) / norm);
            }
        }
    }

    @Override
    protected void buildModel() throws Exception {
        for (int iter = 1; iter <=numIters; iter++) {

            // SGD Optimization

            loss = 0;
            for (TensorEntry te : trainTensor) {
                int[] keys = te.keys();
                double rate = te.get();

                if (rate <= 0)
                    continue;

                double pred = predict(keys);
                double e = rate - pred;

                loss += e * e;

                for (int f = 0; f < numFactors; f++) {

                    double sgd = 1;
                    for (int dd = 0; dd < numDimensions; dd++) {
                        sgd *= M[dd].get(keys[dd], f);
                    }

                    for (int d = 0; d < numDimensions; d++) {
                        double df = M[d].get(keys[d], f);

                        double gdf = sgd / df * e;
                        M[d].add(keys[d], f, lRate * (gdf - reg * df));

                        loss += reg * df * df;
                    }
                }
            }

            loss *= 0.5;
            if (isConverged(iter))
                break;
        }
    }


    @Override
    protected double predict(int u, int j, int c) throws Exception {
        double pred = 0;
        int[] keys = getKeys(u,j,c);

        for (int f = 0; f < numFactors; f++) {

            double prod = 1;
            for (int d = 0; d < numDimensions; d++) {
                prod *= M[d].get(keys[d], f);
            }

            pred += prod;
        }

        if (pred > maxRate)
            pred = maxRate;
        if (pred < minRate)
            pred = minRate;

        return pred;
    }

    protected double predict(int[] keys) {
        double pred = 0;

        for (int f = 0; f < numFactors; f++) {

            double prod = 1;
            for (int d = 0; d < numDimensions; d++) {
                prod *= M[d].get(keys[d], f);
            }

            pred += prod;
        }

        return pred;
    }
}

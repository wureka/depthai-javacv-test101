package org.example;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.Mat;

import static org.bytedeco.opencv.global.opencv_core.*;

/**
 * This class is copied from {@link org.bytedeco.javacv.OpenCVFrameConverter.ToMat()}
 * But I changed a little
 */
public class MyJavaCvConverter {
    // private Mat mat;
    public static boolean  isEqual(Frame frame, Mat mat) {
        return mat != null && frame != null && frame.image != null && frame.image.length > 0
                && frame.imageWidth == mat.cols() && frame.imageHeight == mat.rows()
                && frame.imageChannels == mat.channels() && getMatDepth(frame.imageDepth) == mat.depth()
                && new Pointer(frame.image[0].position(0)).address() == mat.data().address()
                && frame.imageStride * Math.abs(frame.imageDepth) / 8 == (int)mat.step();
    }
    public static int getMatDepth(int depth) {
        switch (depth) {
            case Frame.DEPTH_UBYTE:  return CV_8U;
            case Frame.DEPTH_BYTE:   return CV_8S;
            case Frame.DEPTH_USHORT: return CV_16U;
            case Frame.DEPTH_SHORT:  return CV_16S;
            case Frame.DEPTH_FLOAT:  return CV_32F;
            case Frame.DEPTH_INT:    return CV_32S;
            case Frame.DEPTH_DOUBLE: return CV_64F;
            default:  return -1;
        }
    }

    public static Mat convertToMat(Frame frame) {
        Mat mat = null;
        if (frame == null || frame.image == null) {
            return null;
        } else if (frame.opaque instanceof Mat) {
            return (Mat)frame.opaque;
        } else if (!isEqual(frame, mat)) {
            int depth = getMatDepth(frame.imageDepth);
            if (mat != null) {
                mat.releaseReference();
            }
            mat = depth < 0 ? null : (Mat)new Mat(frame.imageHeight, frame.imageWidth, CV_MAKETYPE(depth, frame.imageChannels),
                    new Pointer(frame.image[0].position(0)), frame.imageStride * Math.abs(frame.imageDepth) / 8).retainReference();
        }
        // log.debug("mat cols: {}", mat.cols() );
        return mat;
    }

}

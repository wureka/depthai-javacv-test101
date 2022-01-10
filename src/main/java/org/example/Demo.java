package org.example;

import org.bytedeco.depthai.*;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.PointerScope;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.io.File;

import static org.bytedeco.opencv.global.opencv_highgui.imshow;
import static org.bytedeco.opencv.global.opencv_highgui.waitKey;

public class Demo {
    static boolean INTERLEAVED = false;
    static int WIDTH = 416, HEIGHT = 416;
    /**
     * If you set useJavaCvCanvasFrame as true, it will use CanvasFrame of JavaCV to show images.
     * If you set useJavaCvCanvasFrame as false, it will use imshow of opencv to show images
     */
    static boolean useJavaCvCanvasFrame = true;

    /**
     * Grab image from OAK-D (with external power) and show on screen with JavaCV
     */
    static void demoDepthaiRgbPreview() {
        OpenCVFrameConverter.ToMat converter = null;
        CanvasFrame videoCanvas = null;
        Pipeline pipeline  = new Pipeline();

        ColorCamera camRgb = pipeline.createColorCamera();
        XLinkOut xoutRgb   = pipeline.createXLinkOut();

        xoutRgb.setStreamName("rgb");
        camRgb.setResolution(ColorCameraProperties.SensorResolution.THE_1080_P);
        camRgb.setPreviewSize(WIDTH, HEIGHT);
        camRgb.setInterleaved(INTERLEAVED);
        camRgb.setColorOrder(ColorCameraProperties.ColorOrder.BGR);

        camRgb.preview().link(xoutRgb.input());

        Device device = new Device();
        System.out.println("MX ID:" + device.getMxId().getString());
        device.startPipeline(pipeline);
        IntPointer cameras = device.getConnectedCameras();
        System.out.println("Check point 00001----------------");
        // below leads to:
        //        Stack trace (most recent call last) in thread 96998:
        //        #0    Object "[0x7fbab12927ad]", at 0x7fbab12927ad, in
        //        Segmentation fault (Address not mapped to object [(nil)])
        System.out.printf("Detect %s camera(s) \n", cameras);
        System.out.println("Check point 00002----------------");
        System.out.printf("USB speed: %s \n", device.getUsbSpeed());
        System.out.println("Check point 00003----------------");
        for (int i = 0; i < cameras.limit(); i++) {
            System.out.printf("    Camera %d is ready!\n", cameras.get(i));
        }
        System.out.println("Check point 00004----------------");
        DataOutputQueue qRgb = device.getOutputQueue("rgb", 4, false);

        Mat frame;
        long time0 = 0, time1 = 0, accumConvertTime = 0;
        int count = 0;
        final int TOTAL_FRAME_COUNT = 1000;
        if (useJavaCvCanvasFrame) {
            System.out.println("Initialize JavaCV converter");
            converter = new OpenCVFrameConverter.ToMat();
            System.out.println("Initialize JavaCV CanvasFrame");
            // TODO: Below will happen errors
            videoCanvas = new CanvasFrame("Preview", 1.0);

        }

        System.out.println("Ready to show grabbed frames .....");
        while (true) {
            try (PointerScope scope = new PointerScope()) {
                ImgFrame imgFrame = qRgb.getImgFrame();
                if (imgFrame != null && !imgFrame.isNull()) {
                    time0 = System.currentTimeMillis();
                    frame = imgFrame.getCvFrame();
                    time1 = System.currentTimeMillis();
                    accumConvertTime += (time1 - time0);
                    count++;
                    if (useJavaCvCanvasFrame) {
                        videoCanvas.showImage(converter.convert(frame));
                    } else {
                        imshow("preview", frame);
                    }

                    if (count >=TOTAL_FRAME_COUNT) break;
                    int key = waitKey(1);
                    if (key == 'q' || key == 'Q') {
                        break;
                    }
                } else {
                    System.out.println("Not ImgFrame");
                }
            }

        }
        device.close();
        pipeline.close();
    }

    /**
     * Read a video file and show it
     */
    static void demoJavaCv() throws Exception{
        File videoFile = new File("foo.mp4");
        FFmpegFrameGrabber ffmpegFrameGrabber = new FFmpegFrameGrabber(videoFile);
        CanvasFrame canvasFrame = new CanvasFrame("Some Title", 1.0);

        ffmpegFrameGrabber.start();
        for(int x = 0; x < 10000; x++) {
            Frame frame = ffmpegFrameGrabber.grabFrame();
            canvasFrame.showImage(frame);
        }
        ffmpegFrameGrabber.stop();
        ffmpegFrameGrabber.close();
    }


    public static void main(String[] args) throws Exception {
        // demoJavaCv();
        demoDepthaiRgbPreview();
    }
}

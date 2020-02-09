package kr.soen.myapplication;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


/**
 * Created by esc on 2018-01-10.
 */

public class SecondActivity extends AppCompatActivity {

    private Button mRecordBtn, mLoginBtn;

    private AudioRecord mRecorder = null;
    private Thread mRecordingThread = null;
    private BufferedInputStream mBIStream;
    private BufferedOutputStream mBOStream;

    private boolean mIsRecording = false;           // 녹음 중인지에 대한 상태값

    private int bufferSize;

    // 설정할 수 있는 sampleRate, AudioFormat, channelConfig 값들을 정의
    private final int[] mSampleRates1 = new int[] {8000};
    private final short[] mAudioFormats1 = new short[] {AudioFormat.ENCODING_PCM_16BIT};
    private final short[] mChannelConfigs1 = new short[] {AudioFormat.CHANNEL_IN_MONO};

    // 위의 값들 중 실제 녹음 및 재생 시 선택된 설정값들을 저장
    private int mSampleRate;
    private short mAudioFormat;
    private short mChannelConfig;

    private String mPath = "";                      // 녹음한 파일을 저장할 경로
    private final int mBufferSize = 1024;
    private final int mBytesPerElement = 2;

    private final String TEMP_FILE_NAME = "test_temp.bak";
    private int mAudioLen = 0;
    private final int WAVE_CHANNEL_MONO = 1;  //wav 파일 헤더 생성시 채널 상수값
    private final int HEADER_SIZE = 0x2c;
    private final int RECORDER_BPP = 16;
    private final int RECORDER_SAMPLERATE = 8000;

    private static int maxDataNum = 512;

    final static int W = 256; //frame duration(32ms) 104 frame shift(13ms) no zero-padding
    public NeuralNetwork neuralNetwork = new NeuralNetwork(this); //추가 1
    public NeuralNetwork2 neuralNetwork2 = new NeuralNetwork2(this); //추가 1
    public double[][] sourceData = new double[maxDataNum][13]; // 소스데이터배열 추가
    public int dataNum=0;

    public static int loginnum = 0;
    public static int nextnum = 0;

    private ThirdActivity third = new ThirdActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page2);

        mRecordBtn = (Button)findViewById(R.id.start);
        mLoginBtn = (Button)findViewById(R.id.login);

        mRecordBtn.setOnClickListener(btnClick);
        mLoginBtn.setOnClickListener(btnClick);

        mLoginBtn.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        System.out.println("mLoginBtn loginnum : " + loginnum);
                        String str = "";

                        try {
                            if(loginnum==1){
                                str = neuralNetwork.NN(sourceData, dataNum);
                                System.out.println("남혁준뉴럴");
                                System.out.println("str : " + str);
                            }
                            else if(loginnum == 2){
                                str = neuralNetwork2.NN(sourceData, dataNum);
                                System.out.println("조은지뉴럴");
                                System.out.println("str : " + str);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                       if(str.equals("accept")){
                           Intent intent = new Intent(getApplicationContext(), ThirdActivity.class);
                            startActivity(intent);
                            third.checkID(loginnum);
                       }
                       else if(str.equals("reject"))
                           Toast.makeText(SecondActivity.this,str+"!!",Toast.LENGTH_LONG).show();

                    }
                }
        );
    }

    public void checkID(int num){
        loginnum = num;
    }


    // 녹음을 수행할 Thread를 생성하여 녹음을 수행하는 함수
    private void startRecording() {

        mRecorder = findAudioRecord(); // 여기서 함수사용해서 설정값으로 잘 설정된 Recorder를 생성하고

        mRecorder.getSampleRate();

        mRecorder.startRecording();

        mIsRecording = true;

        mRecordingThread = new Thread(new Runnable() {

            @Override

            public void run() {
                writeAudioDataToFile2();
            }

        }, "AudioRecorder Thread");

        mRecordingThread.start();

    }

    // 녹음을 하기 위한 sampleRate, audioFormat, channelConfig 값들을 설정
    private AudioRecord findAudioRecord() {

        for (int rate : mSampleRates1) {

            for (short format : mAudioFormats1) {

                for (short channel : mChannelConfigs1) {

                    try {

                        bufferSize = AudioRecord.getMinBufferSize(rate, channel, format); // bufferSize = 640

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {

                            mSampleRate = rate;

                            mAudioFormat = format;

                            mChannelConfig = channel;

                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, mSampleRate, mChannelConfig, mAudioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) { //여기서 설정값이 적절하면 return해주니까

                                return recorder;    // 적당한 설정값들로 생성된 Recorder 반환

                            }

                        }

                    } catch (Exception e) {

                        e.printStackTrace();

                    }

                }

            }

        }

        return null;                     // 적당한 설정값들을 찾지 못한 경우 Recorder를 찾지 못하고 null 반환

    }

    private void writeAudioDataToFile() {

        String sd = Environment.getExternalStorageDirectory().getAbsolutePath();

        mPath = sd + "/record_audiorecord.pcm";



        short sData[] = new short[mBufferSize];

        FileOutputStream fos = null;



        try {

            fos = new FileOutputStream(mPath);

            while (mIsRecording) {

                mRecorder.read(sData, 0, mBufferSize);

                byte bData[] = short2byte(sData);

                fos.write(bData, 0, mBufferSize * mBytesPerElement);

            }

            fos.close();

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    private void writeAudioDataToFile2() {

        byte[] buffer = new byte[bufferSize];
        byte[] data = new byte[bufferSize];

        File waveFile = new File(Environment.getExternalStorageDirectory()+"/"+"test.wav");

        File tempFile = new File(Environment.getExternalStorageDirectory()+"/"+TEMP_FILE_NAME);


        try {

            mBOStream = new BufferedOutputStream(new FileOutputStream(tempFile));

        } catch (FileNotFoundException e1) {

            // TODO Auto-generated catch block

            e1.printStackTrace();

        }

        int read = 0;
        int len = 0;

        if (null != mBOStream) {
            try {
                while (mIsRecording) {
                    read = mRecorder.read(data, 0, bufferSize);
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        mBOStream.write(data);
                    }
                }

                mBOStream.flush();

                mAudioLen = (int)tempFile.length();

                mBIStream = new BufferedInputStream(new FileInputStream(tempFile));

                mBOStream.close();

                mBOStream = new BufferedOutputStream(new FileOutputStream(waveFile));

                mBOStream.write(getFileHeader());

                len = HEADER_SIZE;

                while ((read = mBIStream.read(buffer)) != -1) {

                    mBOStream.write(buffer);

                }


                mBOStream.flush();

                mBIStream.close();

                mBOStream.close();



            } catch (IOException e1) {

                // TODO Auto-generated catch block

                e1.printStackTrace();

            }

        }
    }

    private byte[] getFileHeader() {

        byte[] header = new byte[HEADER_SIZE];

        int totalDataLen = mAudioLen + 40;

        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * WAVE_CHANNEL_MONO/8;

        header[0] = 'R';  // RIFF/WAVE header

        header[1] = 'I';

        header[2] = 'F';

        header[3] = 'F';

        header[4] = (byte) (totalDataLen & 0xff);

        header[5] = (byte) ((totalDataLen >> 8) & 0xff);

        header[6] = (byte) ((totalDataLen >> 16) & 0xff);

        header[7] = (byte) ((totalDataLen >> 24) & 0xff);

        header[8] = 'W';

        header[9] = 'A';

        header[10] = 'V';

        header[11] = 'E';

        header[12] = 'f';  // 'fmt ' chunk

        header[13] = 'm';

        header[14] = 't';

        header[15] = ' ';

        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk

        header[17] = 0;

        header[18] = 0;

        header[19] = 0;

        header[20] = (byte)1;  // format = 1 (PCM방식)

        header[21] = 0;

        header[22] =  WAVE_CHANNEL_MONO;

        header[23] = 0;

        header[24] = (byte) (RECORDER_SAMPLERATE & 0xff);

        header[25] = (byte) ((RECORDER_SAMPLERATE >> 8) & 0xff);

        header[26] = (byte) ((RECORDER_SAMPLERATE >> 16) & 0xff);

        header[27] = (byte) ((RECORDER_SAMPLERATE >> 24) & 0xff);

        header[28] = (byte) (byteRate & 0xff);

        header[29] = (byte) ((byteRate >> 8) & 0xff);

        header[30] = (byte) ((byteRate >> 16) & 0xff);

        header[31] = (byte) ((byteRate >> 24) & 0xff);

        header[32] = (byte) RECORDER_BPP * WAVE_CHANNEL_MONO/8;  // block align

        header[33] = 0;

        header[34] = RECORDER_BPP;  // bits per sample

        header[35] = 0;

        header[36] = 'd';

        header[37] = 'a';

        header[38] = 't';

        header[39] = 'a';

        header[40] = (byte)(mAudioLen & 0xff);

        header[41] = (byte)((mAudioLen >> 8) & 0xff);

        header[42] = (byte)((mAudioLen >> 16) & 0xff);

        header[43] = (byte)((mAudioLen >> 24) & 0xff);

        return header;

    }

    // 녹음을 중지하는 함수
    private void stopRecording() {

        if (mRecorder != null) {

            mIsRecording = false;

            mRecorder.stop();

            mRecorder.release();

            mRecorder = null;

            mRecordingThread = null;

        }

    }

    private View.OnClickListener btnClick = new View.OnClickListener() {

        @Override

        public void onClick(View v) {

            switch (v.getId()) {

                // 녹음 버튼일 경우 녹음 중이지 않을 때는 녹음 시작, 녹음 중일 때는 녹음 중지로 텍스트 변경

                case R.id.start:

                    if (mIsRecording == false) {

                        startRecording();

                        mIsRecording = true;

                        //mRecordBtn.setText("Stop Recording");


                    } else {

                        stopRecording();

                        WavTo();

                        mIsRecording = false;

                        //mRecordBtn.setText("Start Recording");

                    }

                    break;

                /*case R.id.login:
                    String str = "";

                    System.out.println("login num :" + loginnum);
                        try {
                            if(loginnum==1){
                                str = neuralNetwork.NN(sourceData, dataNum);
                                System.out.println("남혁준뉴럴");
                                System.out.println("str : " + str);
                            }
                            else if(loginnum == 2){
                                str = neuralNetwork2.NN(sourceData, dataNum);
                                System.out.println("조은지뉴럴");
                                System.out.println("str : " + str);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    if(str.equals("reject"))
                        nextnum = 0;

                    Toast.makeText(SecondActivity.this,str+"!!",Toast.LENGTH_LONG).show();*/
            }

        }



    };

    public void WavTo(){
        double[] arr = new double[W*2];
        double[] realarr = new double[W];
        double[] imagarr = new double[W];
        File AudioFile = new File(Environment.getExternalStorageDirectory()+"/"+"test.wav");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in;
        byte[] data = new byte[512];
        System.out.println("data길이: "+data.length);;
        double[] inbuf = new double[W];
        double[] fftbuf = new double[W];

        String txtname = Environment.getExternalStorageDirectory().getAbsolutePath()+"/TestLog";

        try {
            in = new BufferedInputStream(new FileInputStream(AudioFile));
            System.out.println("in.available: "+in.available());;
            int read;
            while ((read = in.read(data)) > 0) { //in(AudioFile에서 데이터를 읽어서 data라는 버퍼에 넣어라
                out.write(data, 0, read); //data 배열에 0(시작)부터 read길이만큼읽어라
            }

            out.flush(); //버퍼에 차있던거 방출
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int loopcnt=0;
        int num=0;
        int go=0;
        double threshold = 5;
        data = out.toByteArray();
        System.out.println("bytedata: "+data.length);
        System.out.println("inbuflength: "+inbuf.length);
        MFCC m = new MFCC(W, 13, 20, 8000);
        for(int j=0; j< (data.length)/2 - W; j+=104) //<<<<<<<<<<<메인루프 작성 전체프레임에서 104씩 frame shift
        {
            loopcnt++;
            decode(data, inbuf, j);
            arr = fft(inbuf, fftbuf);
            for (int i = 0; i < arr.length; i++) {
                if((i%2)==0)
                {
                    realarr[i/2] = arr[i];
                }
                else {
                    imagarr[(i-1)/2] = arr[i];
                }
            }
            double sum=0;
            for(int i=0; i<arr.length/2;i++)
            {sum +=imagarr[i]*imagarr[i];}

            if(sum>threshold || go==1) {
                go=1;
                double[] cb = m.cepstrum(realarr, imagarr);
                for (int k = 0; k < cb.length; k++) {
                    sourceData[num][k] = cb[k];
                    WriteTextFile(txtname, "mfcc.txt", String.valueOf(cb[k])+" ");
                }
                num++;
                System.out.println(num+" sum: "+sum);
                WriteTextFile(txtname, "mfcc.txt", "\n");
            }
        }//<<<<<<<<<end loop
        dataNum = num;
}

    //텍스트내용을 경로의 텍스트 파일에 쓰기
    public void WriteTextFile(String foldername, String filename, String contents){
        try{
            File dir = new File (foldername);
            //디렉토리 폴더가 없으면 생성함
            if(!dir.exists()){
                dir.mkdir();
            }
            //파일 output stream 생성
            FileOutputStream fos = new FileOutputStream(foldername+"/"+filename, true);
            //파일쓰기
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(contents);
            writer.flush();

            writer.close();
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void decode(byte[] input, double[] output,int shift) {
        assert input.length == 2 * output.length;
        for (int i = 0; i < output.length; i++) {
            output[i] = (short) (((0xFF & input[2 * (i+shift) + 1]) << 8) | (0xFF & input[2 * (i+shift)]));
            output[i] /= Short.MAX_VALUE;
        }
    }

    public static double[] fft(final double[] inputReal, double[] inputImag) {
        assert inputReal.length == 2 * inputImag.length;
        int n = inputReal.length;
        double ld = Math.log(n) / Math.log(2.0);

        if (((int) ld) - ld != 0) {
            System.out.println("The number of elements is not a power of 2.");
        }
        int nu = (int) ld;
        int n2 = n / 2;
        int nu1 = nu - 1;
        double[] xReal = new double[n];
        double[] xImag = new double[n];
        double tReal, tImag, p, arg, c, s;

        double constant;
        if (true){
            constant = -2 * Math.PI;
        }

        for (int i = 0; i < n; i++) {
            xReal[i] = inputReal[i];
            xImag[i] = inputImag[i];
        }

        int k = 0;
        for (int l = 1; l <= nu; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    p = bitreverseReference(k >> nu1, nu);
                    arg = constant * p / n;
                    c = Math.cos(arg);
                    s = Math.sin(arg);
                    tReal = xReal[k + n2] * c + xImag[k + n2] * s;
                    tImag = xImag[k + n2] * c - xReal[k + n2] * s;
                    xReal[k + n2] = xReal[k] - tReal;
                    xImag[k + n2] = xImag[k] - tImag;
                    xReal[k] += tReal;
                    xImag[k] += tImag;
                    k++;
                }
                k += n2;
            }
            k = 0;
            nu1--;
            n2 /= 2;
        }

        k = 0;
        int r;
        while (k < n) {
            r = bitreverseReference(k, nu);
            if (r > k) {
                tReal = xReal[k];
                tImag = xImag[k];
                xReal[k] = xReal[r];
                xImag[k] = xImag[r];
                xReal[r] = tReal;
                xImag[r] = tImag;
            }
            k++;
        }

        double[] newArray = new double[xReal.length * 2];
        double radice = 1 / Math.sqrt(n);
        for (int i = 0; i < newArray.length; i += 2) {
            int i2 = i / 2;
            newArray[i] = xReal[i2] * radice;
            newArray[i + 1] = xImag[i2] * radice;
        }

    /*   for (int i = 0; i < newArray.length; i++) {
            System.out.println("Array: " + newArray[i]+"count: "+i);
        }*/
        return newArray;

    }

    private static int bitreverseReference(int j, int nu) {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= nu; i++) {
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
        }
        return k;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            finish();

        }

        return super.onKeyDown(keyCode, event);

    }

    // short array형태의 data를 byte array형태로 변환하여 반환하는 함수
    private byte[] short2byte(short[] sData) {

        int shortArrsize = sData.length;

        byte[] bytes = new byte[shortArrsize * 2];

        for (int i = 0; i < shortArrsize; i++) {

            bytes[i * 2] = (byte) (sData[i] & 0x00FF);

            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);

            sData[i] = 0;

        }

        return bytes;

    }

    // 녹음할 때 설정했던 값과 동일한 설정값들로 해당 파일을 재생하는 함수
    private void playWaveFile() {

        int minBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelConfig, mAudioFormat);

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, mSampleRate, mChannelConfig, mAudioFormat, minBufferSize, AudioTrack.MODE_STREAM);

        int count = 0;

        byte[] data = new byte[mBufferSize];



        try {

            FileInputStream fis = new FileInputStream(mPath);

            DataInputStream dis = new DataInputStream(fis);

            audioTrack.play();



            while ((count = dis.read(data, 0, mBufferSize)) > -1) {

                audioTrack.write(data, 0, count);

            }

            audioTrack.stop();

            audioTrack.release();

            dis.close();

            fis.close();

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

}

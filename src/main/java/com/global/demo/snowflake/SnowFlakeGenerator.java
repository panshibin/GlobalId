package com.global.demo.snowflake;

/**
 * @author zby
 * @version 1.0
 * @description: 雪花算法
 * @date 2021/2/21 下午4:51
 */
public class SnowFlakeGenerator {
    /**
     * 机房ID
     */
    private long roomId;
    /**
     * 机器ID
     */
    private long wokerId;
    /**
     * 占用5个bit位
     */
    private long roomIdBit = 5L;
    /**
     * 占用5个bit位
     */
    private long wokerIdBit = 5L;
    /**
     * 12bit递增序列
     */
    private long sequenceBits = 12L;

    /**
     * 声明roomId最大的正整数(最大存储32个机房)
     */
    private long maxRoomId = -1L ^ (-1L << roomIdBit);
    private long maxWokerId = -1L ^ (-1L << wokerIdBit);
    /**
     * 记录sequence能够存储的最大长度
     */
    private long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 设置初始时间的值
     */
    private final static long START_TIME = 1613898948942L;

    /**
     * 递增开始的序列
     */
    private long sequence;
    /**
     * 存储上一次生成的Id的时间戳
     */
    private long lastTimeStamp = -1L;

    private long wokerIdShift = sequenceBits;
    private long roomIdShift = sequenceBits + wokerIdBit;
    private long timeStampShift = sequenceBits + wokerIdBit + roomIdBit;

    public SnowFlakeGenerator(long roomId, long wokerId, long sequence) {
        if (wokerId > maxWokerId || wokerId < 0) {
            throw new IllegalArgumentException("wokerder Id error");
        }
        if (roomId > maxRoomId || roomId < 0) {
            throw new IllegalArgumentException("room Id error");
        }
        if (sequence > sequenceMask || sequence < 0) {
            throw new IllegalArgumentException("sequence error");
        }
        this.roomId = roomId;
        this.wokerId = wokerId;
        this.sequence = sequence;
    }

    /**
     * 生成下个递增序列
     * @param evenNumber true 偶数；false 奇数
     * @return
     */
    public synchronized long nextVal(boolean evenNumber) {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis < lastTimeStamp) {
            throw new RuntimeException("时间戳异常");
        }
        if (lastTimeStamp == currentTimeMillis) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0L) {
                currentTimeMillis = waitToNextMills(lastTimeStamp);
            }
        } else {
            /**
             * true：如果进入到了新的时间毫秒，sequence从0开始
             * false：相同毫秒内，序列号随机获取
             */
            sequence = evenNumber ?  sequence = 0L : currentTimeMillis & 1;
        }
        lastTimeStamp = currentTimeMillis;
        return ((currentTimeMillis - START_TIME) << timeStampShift | (roomId << roomIdShift) | (wokerId << wokerIdShift) | sequence);
    }

    private long waitToNextMills(long lastTimeStamp) {
        long timeMillis = System.currentTimeMillis();
        while (timeMillis <= lastTimeStamp) {
            timeMillis = System.currentTimeMillis();
        }
        return timeMillis;
    }

    public static void main(String[] args) throws InterruptedException {
        SnowFlakeGenerator snowFlaskGenerator = new SnowFlakeGenerator(1, 1, 1);
        for (int i = 0; i < 8; i++) {
            /**
             * 测试一：时间连续，齐偶数都有
             * 705700640722944
             * 705700644917248
             * 705700644917249
             * 705700644917250
             * 705700644917251
             * 705700644917252
             * 705700644917253
             * 705700644917254
             */
//            System.out.println(snowFlaskGenerator.nextVal(true));

            /**
             * 测试二：时间不连续，都是偶数
             * 707110874779648
             * 707110878973952
             * 707110883168256
             * 707110891556864
             * 707110895751168
             * 707110899945472
             * 707110908334080
             * 707110912528384
             */
//            Thread.sleep(1);
//            System.out.println(snowFlaskGenerator.nextVal(true));

            /**
             * 测试三：时间不连续，齐偶数都有
             * 708009957396480
             * 708009965785088
             * 708009969979393
             * 708009974173696
             * 708009982562304
             * 708009986756609
             * 708009990950912
             * 708009995145217
             */
            Thread.sleep(1);
            System.out.println(snowFlaskGenerator.nextVal(false));

        }
    }
}



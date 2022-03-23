package com.sheep.community.service;

import com.sheep.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate redisTemplate;

    private final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    public void recordUV(String ip) {
        String uvKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKey, ip);
    }

    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        List<String> keys = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String uvKey = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keys.add(uvKey);
            calendar.add(Calendar.DATE, 1);
        }

        String unionKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(unionKey, keys.toArray());

        return redisTemplate.opsForHyperLogLog().size(unionKey);
    }

    public void recordDAU(int userId) {
        String dauKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(dauKey, userId, true);
    }

    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        List<byte[]> keys = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String dauKey = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keys.add(dauKey.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(StandardCharsets.UTF_8),
                        keys.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
        if (obj != null) {
            return (long) obj;
        } else {
            return 0;
        }
    }


}

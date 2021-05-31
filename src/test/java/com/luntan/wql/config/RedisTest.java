package com.luntan.wql.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class RedisTest {


    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString() {

        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey,1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));

    }


    @Test
    public void TestHashes() {
        String redisKey = "test:user";


        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"username","zhangsan");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }


    @Test
    public void TestList() {
        String redisKey = "test:id";

        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);


        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,0));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));

    }



    @Test
    public void testSets() {
        String redisKey = "test:teachers";


        redisTemplate.opsForSet().add(redisKey,"刘备","光宇");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testSortedset() {
        String rediskey = "test:students";


        redisTemplate.opsForZSet().add(rediskey,"别龙马",80);
        redisTemplate.opsForZSet().add(rediskey,"龙马",90);
        redisTemplate.opsForZSet().add(rediskey,"马",96);
        redisTemplate.opsForZSet().add(rediskey,"sx",60);


        System.out.println(redisTemplate.opsForZSet().zCard(rediskey));
        System.out.println(redisTemplate.opsForZSet().score(rediskey,"马"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(rediskey,"马"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(rediskey,0,2));





    }

    @Test
    public void testKeys() {
        redisTemplate.delete("test:user");

        System.out.println(redisTemplate.hasKey("test:user"));

        //设置过期时间
        redisTemplate.expire("test:students",10, TimeUnit.SECONDS);

    }

    //多次访问同一个key
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());


    }

    //编程式事务
    @Test
    public void testTransactional() {
    Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                String redisKey = "test:tx";

                redisOperations.multi();


                redisOperations.opsForSet().add(redisKey,"zhangsan");
                redisOperations.opsForSet().add(redisKey,"lisi");
                redisOperations.opsForSet().add(redisKey,"wangwu");

                System.out.println(redisOperations.opsForSet().members(redisKey));


                //提交
                return redisOperations.exec();


            }
        });
    System.out.println(obj);
    }

}
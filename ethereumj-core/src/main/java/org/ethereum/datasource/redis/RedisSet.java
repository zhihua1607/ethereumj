package org.ethereum.datasource.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static org.ethereum.util.Functional.Consumer;
import static org.ethereum.util.Functional.Function;

public class RedisSet<T> extends RedisStorage<T> implements Set<T> {

    RedisSet(String namespace, JedisPool pool, RedisSerializer<T> serializer) {
        super(namespace, pool, serializer);
    }

    @Override
    public int size() {
        return pooledWithResult(new Function<Jedis, Integer>() {
            @Override
            public Integer apply(Jedis jedis) {
                return jedis.scard(getName()).intValue();
            }
        });
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(final Object o) {
        return pooledWithResult(new Function<Jedis, Boolean>() {
            @Override
            public Boolean apply(Jedis jedis) {
                return jedis.sismember(getName(), serialize((T) o));
            }
        });
    }

    @Override
    public Iterator<T> iterator() {
        return asCollection().iterator();
    }

    private Collection<T> asCollection() {
        Set<byte[]> members = pooledWithResult(new Function<Jedis, Set<byte[]>>() {
            @Override
            public Set<byte[]> apply(Jedis jedis) {
                return jedis.smembers(getName());
            }
        });
        return deserialize(members);
    }

    @Override
    public Object[] toArray() {
        return asCollection().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return asCollection().toArray(a);
    }

    @Override
    public boolean add(T t) {
        return addAll(Arrays.asList(t));
    }

    @Override
    public boolean remove(Object o) {
        return removeAll(Arrays.asList(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        return pooledWithResult(new Function<Jedis, Boolean>() {
            @Override
            public Boolean apply(Jedis jedis) {
                return jedis.sadd(getName(), serialize(c)) == 1;
            }
        });
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return pooledWithResult(new Function<Jedis, Boolean>() {
            @Override
            public Boolean apply(Jedis jedis) {
                return jedis.sinterstore(getName(), serialize(c)) == c.size();
            }
        });
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return pooledWithResult(new Function<Jedis, Boolean>() {
            @Override
            public Boolean apply(Jedis jedis) {
                return jedis.srem(getName(), serialize(c)) == 1;
            }
        });
    }

    @Override
    public void clear() {
        pooled(new Consumer<Jedis>() {
            @Override
            public void accept(Jedis jedis) {
                jedis.del(getName());
            }
        });
    }
}

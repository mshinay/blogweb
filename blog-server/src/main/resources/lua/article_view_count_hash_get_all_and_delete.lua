local key = KEYS[1]
local values = redis.call('HGETALL', key)

if #values > 0 then
    redis.call('DEL', key)
end

return values

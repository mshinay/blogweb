local results = {}

for i = 1, #KEYS do
    local current = redis.call('GET', KEYS[i])
    if current then
        redis.call('DEL', KEYS[i])
        results[i] = current
    else
        results[i] = ''
    end
end

return results

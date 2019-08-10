car = {
  "brand": "Ford",
  "model": "Mustang",
  "year": 1964
}

car.update({"brand": "White"})


def foo(dict):
    dict.update({"brand": "Black"})

foo(car)
print(car)

print(int('00008'))

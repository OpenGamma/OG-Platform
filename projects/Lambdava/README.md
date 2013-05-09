Lambdava
=====
Brings some functional goodness to Java.

Lambdava. Short for "Lambda flavoured Java".
Pronounced "λ-va".


Overview
--------


        List<String> list = Arrays.asList("6", "7", "3", "4", "5", "8", "9", "10", "1", "2");
        
        Lambdava<Integer> result = λ(list)
          // map strings to integer
          .map(new Function1<String, Integer>() {
            @Override
            public Integer execute(String s) {
              return Integer.parseInt(s);
            }
          })
            // filter even integers
          .filter(new Function1<Integer, Boolean>() {
            @Override
            public Boolean execute(Integer integer) {
              return integer % 2 == 0;
            }
          })
            // sort them
          .sort()
            // take last 4 integers
          .last(4)
            // take first 2 integers
          .first(2)
          .each(new Function1<Integer, Object>() {
            @Override
            public Void execute(Integer integer) {
              System.out.println("The integer: " + integer);
              return null;
            }
          });
        
        for (Integer integer : result) {
          System.out.println("We can iterate functional directly " + integer);
        }
        
        System.out.println("Or make it to be a 'real' collection: " + result.asCollection());


Motivation
----------


Links
-----
Some useful project links:

* [User guide](https://github.com/OpenGamma/Lambdava/wiki/User-guide) - a longer user guide
* [Wiki](https://github.com/OpenGamma/Lambdava/wiki/Home) - including an [example](https://github.com/OpenGamma/Lambdava/wiki/Example)
* [Javadoc](http://opengamma.github.com/Lambdava/apidocs/index.html) - the two public classes
* [Issue tracker](https://github.com/OpenGamma/Lambdava/issues) - raise bugs or enhancement requests here
* [Project sponsor](http://developers.opengamma.com/) - the project is supported by OpenGamma

Lambdava is licensed under the Apache License v2.

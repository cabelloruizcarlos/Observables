package crrc.com.observable;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.GroupedObservable;

/**
 * Created by carlos on 10/05/2017.
 */

public class MainActivity extends AppCompatActivity {

    private static final int OUTPUT_LIMIT = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // From the tutorial: http://sglora.com/android-tutorial-sobre-rxjava-i-lo-basico/

        // Basic Observable
        Observable<String> variable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    subscriber.onNext("Hello World");
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });

        // The operators
        // JUST
        // It allow us to create an Observable with and object straight away. He will call to onNext and onComplete so we don't need to do it
        Subscription variable1 = Observable.just("Hello World")
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.print(s);
                    }
                });

        // FROM
        // In the case that the subscriber will be a list instead of a variable we use from to emit an observable per each item in the list
        // Example: It works like a for that print the List of item
        String[] strList = {"First Message", "Second Message", "This is a large message", "Short message"};
        variable1 = Observable.from(strList)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.print(s);
                    }
                });

        // OJO: FROM also calls to onNext and onComplete by itself
        // OJO: We can still use just. The result will be exactly the same
        variable1 = Observable.just(strList[0], strList[1], strList[2], strList[3])
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.print(s);
                    }
                });

        // FILTER
        // Each observable will be send to filter and it will be emitted only if it pass the condition
        // Example: We show only the strings with less than 20 caracters
        variable1 = Observable.from(strList)
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String string) {
                        return string.length() < OUTPUT_LIMIT;
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String string) {
                        System.out.print(string);
                    }
                });

        // MAP
        // It allow us to change the observable into other different observable
        // Example: We show only the number of characters of the strings that has less than 20 letters
        variable1 = Observable.from(strList)
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String string) {
                        return string.length();
                    }
                })
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer lenght) {
                        return lenght < OUTPUT_LIMIT;
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer lenght) {
                        System.out.print(lenght);
                    }
                });

        // ALL
        // It is similar than filter but it receive all the observables and emit one only result(applying a and between each comparision) if all of the observable pass the condition
        // Example: We send all the items of the list to check if any of them is null, then filter will emit the result only if it is True
        variable1 = Observable.from(strList)
                .all(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s != null;
                    }
                })
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean areNotNull) {
                        return areNotNull;
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        System.out.print("All fields are not null");
                    }
                });

        //GROUP BY
        // It create groups of items emitted using a condition defined.
        // Example: The operator receive the array and will emit one observable per different group created all in the object GroupObservable<String, Integer>
        // so it will print two lines if there are Even and Odd numbers or only one if there is only one kind of numbers
        final Integer[] intList = {0, 1, 2, 3, 4};
        variable1 = Observable.from(intList)
                .groupBy(new Func1<Integer, String>() {
                    @Override
                    public String call(Integer integer) {
                        return integer % 2 == 0 ? "Even" : "Odd";
                    }
                })
                .subscribe(new Action1<GroupedObservable<String, Integer>>() {
                    @Override
                    public void call(final GroupedObservable<String, Integer> groupbyResult) {
                        groupbyResult.count().subscribe(new Action1<Integer>() {
                            @Override
                            public void call(Integer count) {
                                System.out.print(String.format("There are %d %s numbers", count, groupbyResult.getKey()));
                            }
                        });
                    }
                });

        // CONTAINS
        // It will check if the list contain the item send as a parameter
        // Example: It will print the result of checking if the item is in the list
        variable1 = Observable.just(intList)
                .contains(5)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        System.out.print("");
                    }
                });

        // EXISTS
        // It will check if in the list exist the item send as a parameter
        // Example: It will print the result of checking if the item exist
        variable1 = Observable.from(strList)
                .exists(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String string) {
                        return string.length() > OUTPUT_LIMIT;
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        System.out.print("Exist an string larger than the maximun permited?" + result);
                    }
                });

        // OJO: FILTER only emit and observable if the condition is true but CONTAINS and EXIST always emit and observable with the result
        // OJO: ALL, CONTAINS and EXIST are blockers(stop the flow) until they receive all the items

        // LIMIT
        // It limit the number of observables emited.
        // Example: Print only the number of items set in its parameter
        variable1 = Observable.from(intList)
                .limit(3)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        System.out.print("Item " + integer);
                    }
                });

        // EMPTY
        // It will check if no items were emitted
        // Example: Print if an item was emitted
        variable1 = Observable.from(new Integer[]{})
                .isEmpty()
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        System.out.print("Is the array empty? " + result);
                    }
                });

        // Operators that works with more than one observable at the same time

        // CONCAT
        // It will join two observable into only one
        // Example: The result will show just one list with the items of the two lists
        final Integer[] intList2 = {4, 5, 6, 7};
        Observable<Integer> variable2 = Observable.from(intList);
        Observable<Integer> variable3 = Observable.from(intList2);
        variable1 = Observable.concat(variable2, variable3)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        System.out.print("Item " + integer);
                    }
                });
        // OJO: If the observables were asynchronous we cannot know which observable will be emitted first but CONCAT will print the items of the first observable
        // first and then the items of the second observable

        // SEQUENCEEQUAL
        // If will emit a boolean observable with the result of comparing if the two observables emit the same items and also if there were emitted in the same order
        variable2 = Observable.from(intList);
        variable3 = Observable.from(intList2);
        variable1 = Observable.sequenceEqual(variable2, variable3)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        System.out.print("Is sequence equal? " + result);
                    }
                });

        // COMBINELATEST
        // It will combine the latest observable emitted by the first observable with all the items of the second observable
        // Example: It will show the sum of the last item in the first observable with one of each item of the second observable.
        variable2 = Observable.from(intList);
        variable3 = Observable.from(intList2);
        variable1 = Observable.combineLatest(variable2, variable3, new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer lastItemFirstObservable, Integer itemSecondObservable) {
                return lastItemFirstObservable + itemSecondObservable;
            }
        })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer result) {
                        System.out.print(" Sum " + result);
                    }
                });
        // OJO :It will be the last item of the first observable because there are synchronous and the second observable will emit
        // items only when the first finished, so the last one emitted by the first observable will be the last

        // ZIP
        // It allow to combine two observables of different kind of items
        // Example: It will combine an Observable<String> with an Observable<Integer>. It will combine them concatenating its values
        variable = Observable.just("First number: ", "Second number: ", "Third number: ", "fourth number: ");
        variable2 = Observable.from(intList);
        variable1 = Observable.zip(variable, variable2, new Func2<String, Integer, String>() {
            @Override
            public String call(String s, Integer integer) {
                return s + integer;
            }
        })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String resutl) {
                        System.out.print(resutl);
                    }
                });

        //OJO: combineLatest and zip need to define a Function to know how to handle the items of the two observables
    }
}

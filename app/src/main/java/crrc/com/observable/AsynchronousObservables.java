package crrc.com.observable;

import android.annotation.SuppressLint;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import java.util.Random;

import crrc.com.observable.model.User;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


/**
 * Created by carlos on 10/05/2017.
 */

public class AsynchronousObservables extends AppCompatActivity {
    private static final String TAG = AsynchronousObservables.class.getSimpleName();
    private CompositeSubscription mCompositeSubscription;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Another operators

        // FLATMAP
        // It works like map but flattering the result. I mean, while map receive one item and return one item, flatmap can return an arbitrary number of values from one item.
        // http://stackoverflow.com/questions/26684562/whats-the-difference-between-map-and-flatmap-methods-in-java-8
        // Example: Receiving as an item the name of the list to print, flatmap will emit an observable with the list with that name
        final Integer[] intList = {0, 1, 2, 3, 4};
        final Integer[] intList2 = {4, 5, 6, 7};
        Observable.just("FirstList", "SecondList")
                .flatMap(new Func1<String, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(String s) {
                        Integer[] result;
                        if (s.equals("FirstList"))
                            result = intList;
                        else
                            result = intList2;

                        return Observable.from(result);
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        System.out.print("Item " + integer);
                    }
                });

        // DELAY
        // It allows to stop the observable some time
        Random r = new Random();
        Subscription variable = Observable.just(intList)
                .delay(r.nextLong(), TimeUnit.SECONDS, Schedulers.newThread())
                .subscribe(new Action1<Integer[]>() {
                    @Override
                    public void call(Integer[] result) {
                        System.out.print("Item " + result);
                    }
                });


        // This is the main example that we will be modifying depending on the next explanations
        // getNumbers return an observable of a list of integers that we change into a boolean answering the question about being each item even or odd
        getNumbers()
                .flatMap(new Func1<Integer[], Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Integer[] numbers) {
                        return Observable.from(numbers);
                    }
                })
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer item) {
                        return item % 2 == 0;
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        System.out.print("Is Odd? " + result);
                    }
                });


        // OBSERVEON
        // We use it to set a point from which the operations will be run on a other thread
        // Example: The println shows in which thread is running each part of the code
        Thread.currentThread().setName("Main Thread");
        getNumbers()
                .observeOn(Schedulers.newThread())
                .flatMap(new Func1<Integer[], Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Integer[] numbers) {
                        System.out.println("FlatMap Thread = " + Thread.currentThread().getName());
                        return Observable.from(numbers);
                    }
                })
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer item) {
                        System.out.println("Map Thread = " + Thread.currentThread().getName());
                        return item % 2 == 0;
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        System.out.println("Subscribe Thread = " + Thread.currentThread().getName());
                        System.out.print("Is Odd? " + result);
                    }
                });

        // SUBSCRIBEON
        // It is used to include even the create of the observable into other thread
        // Example: The println result will show that even the create of the getNumber method is running on the new thread
        getNumbers()
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<Integer[], Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Integer[] numbers) {
                        System.out.println("FlatMap Thread = " + Thread.currentThread().getName());
                        return Observable.from(numbers);
                    }
                })
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer item) {
                        System.out.println("Map Thread = " + Thread.currentThread().getName());
                        return item % 2 == 0;
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        System.out.println("Subscribe Thread = " + Thread.currentThread().getName());
                        System.out.print("Is Odd? " + result);
                    }
                });
        // OJO: The observables by default run in the Main Thread

        // AMB
        // Using two observables, it will emit only the items of the first observable that send it first item
        // Example: It will show only the items of one list. It would change thanks to the combination of Random and Delay
        Observable<Integer> variable2 = Observable.from(intList)
                .subscribeOn(Schedulers.newThread())
                .delay(r.nextLong(), TimeUnit.SECONDS);
        Observable<Integer> variable3 = Observable.from(intList2)
                .subscribeOn(Schedulers.newThread())
                .delay(r.nextLong(), TimeUnit.SECONDS);

        Observable.amb(variable2, variable3)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer result) {
                        System.out.print(" Number " + result);
                    }
                });

        // MERGE
        // It works like CONCAT but instead of emit the observable sorted, MERGE emit the observable in the order that they are being received
        Observable.merge(variable2, variable3)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer result) {
                        System.out.print(" Number " + result);
                    }
                });

        // Observable specific for Android
        // ANDROID SCHEDULERS
        //Example: We set that the observable will run on the Schedulers.io() thread and the subscription on the AndroidSchedulers.mainThread()
        getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        Log.d(TAG, "User = " + user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, throwable.getMessage(), throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "Complete");
                    }
                });

        // COMPOSITE SUBSCRIPTION
        // A memory leak is something that we need to avoid. To do it we should stop the subscriptions before the Activity/Fragment are destroyed.
        // To do it we can add them to a CompositeSubscription and stop them in the onDestroy callback. Example:
        mCompositeSubscription = new CompositeSubscription();
        mCompositeSubscription.add(getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        Log.d(TAG, "User = " + user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, throwable.getMessage(), throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "Complete");
                    }
                }));
    }

    @Override
    protected void onDestroy() {
        mCompositeSubscription.unsubscribe();
        super.onDestroy();
    }

    private static Observable<Integer[]> getNumbers() {
        System.out.println("Function getNumbers Thread = " + Thread.currentThread().getName());
        return Observable.create(new Observable.OnSubscribe<Integer[]>() {
            @Override
            public void call(Subscriber<? super Integer[]> subscriber) {
                System.out.println("Observable.create Thread = " + Thread.currentThread().getName());
                Integer[] numbers = {3, 6, 2};

                try {
                    subscriber.onNext(numbers);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<User> getUser() {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                try {
                    // It should be a call to the server retrieving the user information
                    User user = new User("Carlos", 32);

                    subscriber.onNext(user);
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }
}

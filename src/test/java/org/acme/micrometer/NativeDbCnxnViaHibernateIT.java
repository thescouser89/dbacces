package org.acme.micrometer;

import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativeDbCnxnViaHibernateIT extends DbCnxnViaHibernateTest {

    // Execute the same tests but in native mode.
}
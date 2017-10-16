#include <Qtest>
#include "test_case_test_runner.h"
#include "test_case_lib.h"

test_case_test_runner::test_case_test_runner(QObject *parent) : QObject(parent)
{

}

void test_case_test_runner::test_function_one_here() {

    test_case_lib test_case;

    QVERIFY(!strcmp(test_case.piece_of_string(), "Hello, world!"));

}

void test_case_test_runner::test_function_two_here() {

    test_case_lib test_case;

     QVERIFY(test_case.adding_ints(666, 1337) == 2003);

}
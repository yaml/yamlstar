! Test program for YAMLStar Fortran bindings
program test_yamlstar
  use yamlstar
  implicit none

  type(yamlstar_t) :: ys
  character(len=:), allocatable :: result
  integer :: test_count, pass_count

  test_count = 0
  pass_count = 0

  print *, "YAMLStar Fortran Binding Tests"
  print *, "=============================="
  print *, ""

  ! Initialize YAMLStar
  call ys%init()

  ! Test 1: Version
  test_count = test_count + 1
  print *, "Test 1: Get version"
  result = ys%version()
  if (len(result) > 0) then
    print *, "  PASS: version = ", trim(result)
    pass_count = pass_count + 1
  else
    print *, "  FAIL: Empty version string"
  end if
  print *, ""

  ! Test 2: Load simple scalar
  test_count = test_count + 1
  print *, "Test 2: Load simple scalar"
  result = ys%load("hello")
  if (index(result, '"data"') > 0 .and. index(result, 'hello') > 0) then
    print *, "  PASS: ", trim(result)
    pass_count = pass_count + 1
  else
    print *, "  FAIL: ", trim(result)
  end if
  print *, ""

  ! Test 3: Load integer
  test_count = test_count + 1
  print *, "Test 3: Load integer"
  result = ys%load("42")
  if (index(result, '"data"') > 0 .and. index(result, '42') > 0) then
    print *, "  PASS: ", trim(result)
    pass_count = pass_count + 1
  else
    print *, "  FAIL: ", trim(result)
  end if
  print *, ""

  ! Test 4: Load float
  test_count = test_count + 1
  print *, "Test 4: Load float"
  result = ys%load("3.14")
  if (index(result, '"data"') > 0 .and. index(result, '3.14') > 0) then
    print *, "  PASS: ", trim(result)
    pass_count = pass_count + 1
  else
    print *, "  FAIL: ", trim(result)
  end if
  print *, ""

  ! Test 5: Load boolean true
  test_count = test_count + 1
  print *, "Test 5: Load boolean true"
  result = ys%load("true")
  if (index(result, '"data"') > 0 .and. index(result, 'true') > 0) then
    print *, "  PASS: ", trim(result)
    pass_count = pass_count + 1
  else
    print *, "  FAIL: ", trim(result)
  end if
  print *, ""

  ! Test 6: Load boolean false
  test_count = test_count + 1
  print *, "Test 6: Load boolean false"
  result = ys%load("false")
  if (index(result, '"data"') > 0 .and. index(result, 'false') > 0) then
    print *, "  PASS: ", trim(result)
    pass_count = pass_count + 1
  else
    print *, "  FAIL: ", trim(result)
  end if
  print *, ""

  ! Test 7: Load null
  test_count = test_count + 1
  print *, "Test 7: Load null"
  result = ys%load("null")
  if (index(result, '"data"') > 0 .and. index(result, 'null') > 0) then
    print *, "  PASS: ", trim(result)
    pass_count = pass_count + 1
  else
    print *, "  FAIL: ", trim(result)
  end if
  print *, ""

  ! Test 8: Load simple mapping
  test_count = test_count + 1
  print *, "Test 8: Load simple mapping"
  result = ys%load("key: value")
  if (index(result, '"data"') > 0 .and. index(result, 'key') > 0 &
      .and. index(result, 'value') > 0) then
    print *, "  PASS: ", trim(result)
    pass_count = pass_count + 1
  else
    print *, "  FAIL: ", trim(result)
  end if
  print *, ""

  ! Test 9: Load simple sequence
  test_count = test_count + 1
  print *, "Test 9: Load simple sequence"
  result = ys%load("[a, b, c]")
  if (index(result, '"data"') > 0 .and. index(result, '[') > 0) then
    print *, "  PASS: ", trim(result)
    pass_count = pass_count + 1
  else
    print *, "  FAIL: ", trim(result)
  end if
  print *, ""

  ! Test 10: Load all (multi-document)
  test_count = test_count + 1
  print *, "Test 10: Load all (multi-document)"
  result = ys%load_all("---" // new_line('A') // &
                       "doc1" // new_line('A') // &
                       "---" // new_line('A') // &
                       "doc2")
  if (index(result, '"data"') > 0 .and. index(result, 'doc1') > 0 &
      .and. index(result, 'doc2') > 0) then
    print *, "  PASS: ", trim(result)
    pass_count = pass_count + 1
  else
    print *, "  FAIL: ", trim(result)
  end if
  print *, ""

  ! Cleanup
  call ys%destroy()

  ! Summary
  print *, "=============================="
  print *, "Test Summary:"
  print '(A,I0,A,I0)', "  Passed: ", pass_count, " / ", test_count
  if (pass_count == test_count) then
    print *, "  All tests passed!"
  else
    print '(A,I0,A)', "  FAILED: ", test_count - pass_count, " tests"
    stop 1
  end if

end program test_yamlstar

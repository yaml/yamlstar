! YAMLStar Fortran Bindings
! This file contains both the low-level C bindings and high-level Fortran API

! Low-level C binding interface for libyamlstar
module yamlstar_c
  use, intrinsic :: iso_c_binding
  implicit none

  interface
    ! Create a new GraalVM isolate
    function graal_create_isolate(params, isolate, isolate_thread) &
        bind(C, name='graal_create_isolate') result(rc)
      import :: c_ptr, c_int
      type(c_ptr), value :: params
      type(c_ptr), value :: isolate
      type(c_ptr) :: isolate_thread
      integer(c_int) :: rc
    end function graal_create_isolate

    ! Tear down a GraalVM isolate
    function graal_tear_down_isolate(isolate_thread) &
        bind(C, name='graal_tear_down_isolate') result(rc)
      import :: c_ptr, c_int
      type(c_ptr), value :: isolate_thread
      integer(c_int) :: rc
    end function graal_tear_down_isolate

    ! Load a YAML document
    function yamlstar_load_c(isolate_thread, yaml) &
        bind(C, name='yamlstar_load') result(json_ptr)
      import :: c_ptr, c_char
      type(c_ptr), value :: isolate_thread
      character(kind=c_char), intent(in) :: yaml(*)
      type(c_ptr) :: json_ptr
    end function yamlstar_load_c

    ! Load all YAML documents from a multi-document string
    function yamlstar_load_all_c(isolate_thread, yaml) &
        bind(C, name='yamlstar_load_all') result(json_ptr)
      import :: c_ptr, c_char
      type(c_ptr), value :: isolate_thread
      character(kind=c_char), intent(in) :: yaml(*)
      type(c_ptr) :: json_ptr
    end function yamlstar_load_all_c

    ! Get YAMLStar version string
    function yamlstar_version_c(isolate_thread) &
        bind(C, name='yamlstar_version') result(ver_ptr)
      import :: c_ptr
      type(c_ptr), value :: isolate_thread
      type(c_ptr) :: ver_ptr
    end function yamlstar_version_c
  end interface

end module yamlstar_c


! High-level Fortran API for YAMLStar
module yamlstar
  use, intrinsic :: iso_c_binding
  use yamlstar_c
  implicit none
  private

  public :: yamlstar_t

  ! YAMLStar class/derived type
  type :: yamlstar_t
    private
    type(c_ptr) :: isolate_thread = c_null_ptr
  contains
    procedure :: init => yamlstar_init
    procedure :: destroy => yamlstar_destroy
    procedure :: load => yamlstar_load
    procedure :: load_all => yamlstar_load_all
    procedure :: version => yamlstar_version
  end type yamlstar_t

contains

  ! Initialize YAMLStar and create GraalVM isolate
  subroutine yamlstar_init(this)
    class(yamlstar_t), intent(inout) :: this
    integer(c_int) :: rc

    rc = graal_create_isolate(c_null_ptr, c_null_ptr, this%isolate_thread)
    if (rc /= 0) then
      error stop 'Failed to create GraalVM isolate'
    end if
  end subroutine yamlstar_init

  ! Destroy YAMLStar and tear down GraalVM isolate
  subroutine yamlstar_destroy(this)
    class(yamlstar_t), intent(inout) :: this
    integer(c_int) :: rc

    if (c_associated(this%isolate_thread)) then
      rc = graal_tear_down_isolate(this%isolate_thread)
      if (rc /= 0) then
        error stop 'Failed to tear down GraalVM isolate'
      end if
      this%isolate_thread = c_null_ptr
    end if
  end subroutine yamlstar_destroy

  ! Load a YAML document and return JSON string
  function yamlstar_load(this, yaml) result(json)
    class(yamlstar_t), intent(in) :: this
    character(len=*), intent(in) :: yaml
    character(len=:), allocatable :: json
    type(c_ptr) :: json_ptr
    character(len=:), allocatable :: yaml_c

    if (.not. c_associated(this%isolate_thread)) then
      error stop 'YAMLStar not initialized'
    end if

    ! Convert Fortran string to C string
    yaml_c = f_string_to_c(yaml)

    ! Call C function
    json_ptr = yamlstar_load_c(this%isolate_thread, yaml_c)

    ! Convert C string to Fortran string
    json = c_ptr_to_string(json_ptr)
  end function yamlstar_load

  ! Load all YAML documents and return JSON string
  function yamlstar_load_all(this, yaml) result(json)
    class(yamlstar_t), intent(in) :: this
    character(len=*), intent(in) :: yaml
    character(len=:), allocatable :: json
    type(c_ptr) :: json_ptr
    character(len=:), allocatable :: yaml_c

    if (.not. c_associated(this%isolate_thread)) then
      error stop 'YAMLStar not initialized'
    end if

    ! Convert Fortran string to C string
    yaml_c = f_string_to_c(yaml)

    ! Call C function
    json_ptr = yamlstar_load_all_c(this%isolate_thread, yaml_c)

    ! Convert C string to Fortran string
    json = c_ptr_to_string(json_ptr)
  end function yamlstar_load_all

  ! Get YAMLStar version
  function yamlstar_version(this) result(ver)
    class(yamlstar_t), intent(in) :: this
    character(len=:), allocatable :: ver
    type(c_ptr) :: ver_ptr

    if (.not. c_associated(this%isolate_thread)) then
      error stop 'YAMLStar not initialized'
    end if

    ver_ptr = yamlstar_version_c(this%isolate_thread)
    ver = c_ptr_to_string(ver_ptr)
  end function yamlstar_version

  ! Helper: Convert Fortran string to C string (null-terminated)
  function f_string_to_c(f_string) result(c_string)
    character(len=*), intent(in) :: f_string
    character(len=:), allocatable :: c_string

    ! Append null terminator
    c_string = trim(f_string) // c_null_char
  end function f_string_to_c

  ! Helper: Convert C string pointer to Fortran allocatable string
  function c_ptr_to_string(c_str_ptr) result(f_string)
    type(c_ptr), intent(in) :: c_str_ptr
    character(len=:), allocatable :: f_string
    character(kind=c_char), dimension(:), pointer :: c_str_array
    integer :: i, str_len

    if (.not. c_associated(c_str_ptr)) then
      f_string = ""
      return
    end if

    ! Find string length (search for null terminator)
    str_len = 0
    call c_f_pointer(c_str_ptr, c_str_array, [1])
    do while (c_str_array(str_len + 1) /= c_null_char)
      str_len = str_len + 1
      call c_f_pointer(c_str_ptr, c_str_array, [str_len + 1])
    end do

    ! Allocate and copy
    allocate(character(len=str_len) :: f_string)
    call c_f_pointer(c_str_ptr, c_str_array, [str_len])
    do i = 1, str_len
      f_string(i:i) = c_str_array(i)
    end do
  end function c_ptr_to_string

end module yamlstar

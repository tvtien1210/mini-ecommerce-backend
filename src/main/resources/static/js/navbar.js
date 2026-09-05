// ==================================================
// PAGE LOAD
// ==================================================

document.addEventListener("DOMContentLoaded", async function () {


    // ==================================================
    // GET ELEMENTS
    // ==================================================

    // Lấy vị trí hiển thị thông tin User trên Navbar
    const userInfo = document.getElementById("userInfo");

    // Lấy vị trí hiển thị Admin Menu trên Navbar
    const adminMenu = document.getElementById("adminMenu");


    // ==================================================
    // GET CURRENT USER
    // ==================================================

    // Gọi API /api/auth/me
    // để kiểm tra User hiện tại đã đăng nhập hay chưa
    const user = await getCurrentUser();


    // ==================================================
    // USER CHƯA ĐĂNG NHẬP
    // ==================================================

    if (!user) {
        userInfo.innerHTML = `
            <a class="nav-link text-white" href="/login">
                <i class="bi bi-person-circle"></i>
                Login
            </a>
        `;

        return;
    }


  // ==================================================
  // UPDATE CART BADGE
  // ==================================================

  try {

      // Gọi API lấy Cart hiện tại
      const cartResponse = await apiFetch("/api/cart/my");

      // Nếu API thành công
      if (!cartResponse.ok) {throw new Error("Cart API failed")};

       // Chuyển response thành CartDTO
       const cart = await cartResponse.json();

       // Cập nhật Cart Badge
       updateCartBadge(cart);


  } catch (error) {

      //catch bat xong error o tren throw tren nhung code duoi van chay tiep
      console.error(
          "Failed to load cart:",
          error
      );
  }


    // ==================================================
    // USER ĐÃ ĐĂNG NHẬP
    // ==================================================

    // Lấy username từ CurrentUserDTO
    const username = user.username;

    // Lấy danh sách Role
    const roles = user.roles || [];


    // Kiểm tra User có phải Customer hay không
    const isCustomer = roles.includes("ROLE_CUSTOMER");

    // Kiểm tra User có phải Staff hay không
    const isStaff = roles.includes("ROLE_STAFF");

    // Kiểm tra User có phải Admin hay không
    const isAdmin = roles.includes("ROLE_ADMIN");

    // ==================================================
    // CUSTOMER
    // ==================================================

    // Nếu User là Customer
    if (isCustomer) {

        // Hiển thị tên User và Dropdown Menu
        userInfo.innerHTML = `
            <div class="dropdown">

                <a class="nav-link text-white dropdown-toggle"
                   href="#"
                   data-bs-toggle="dropdown">

                    <i class="bi bi-person-circle me-1"></i>
                    ${username}

                </a>

                <ul class="dropdown-menu dropdown-menu-end dropdown-menu-dark">

                    <li>
                        <a class="dropdown-item text-start" href="#">
                            <i class="bi bi-person me-2"></i>
                            Profile
                        </a>
                    </li>

                    <li>
                        <a class="dropdown-item text-start" href="/myorders">
                            <i class="bi bi-bag-check me-2"></i>
                            My Orders
                        </a>
                    </li>

                    <li>
                        <hr class="dropdown-divider">
                    </li>

                    <li>
                        <a class="dropdown-item text-center"
                           href="#"
                           id="logoutBtn">

                            Logout
                            <i class="bi bi-box-arrow-right ms-2"></i>

                        </a>
                    </li>

                </ul>

            </div>
        `;
    }

    // ==================================================
    // STAFF
    // ==================================================

    // Nếu User là Staff
    if (isStaff) {

        // Hiển thị Staff Menu
        adminMenu.classList.remove("d-none");

        // Ẩn User Info
        userInfo.classList.add("d-none");


        // Tạo Staff Dropdown Menu
        adminMenu.innerHTML = `

            <div class="dropdown">

                <a class="nav-link text-white dropdown-toggle"
                   href="#"
                   role="button"
                   data-bs-toggle="dropdown">

                    <i class="bi bi-person-badge me-1"></i>
                    Staff

                </a>

                <ul class="dropdown-menu dropdown-menu-end dropdown-menu-dark">

                    <li>
                        <a class="dropdown-item" href="/staff">
                            <i class="bi bi-speedometer2 me-2"></i>
                            Staff Dashboard
                        </a>
                    </li>

                    <li>
                        <a class="dropdown-item" href="/staff/orders">
                            <i class="bi bi-cart-check me-2"></i>
                            Manage Orders
                        </a>
                    </li>

                    <li>
                        <hr class="dropdown-divider">
                    </li>

                    <li>
                        <a class="dropdown-item text-center"
                           href="#"
                           id="logoutBtn">

                            Logout
                            <i class="bi bi-box-arrow-right ms-2"></i>

                        </a>
                    </li>

                </ul>

            </div>
        `;
    }


    // ==================================================
    // ADMIN
    // ==================================================

    // Nếu User là Admin
    if (isAdmin) {

        // Hiển thị Admin Menu
        adminMenu.classList.remove("d-none");

        // Ẩn User Info của Customer
        userInfo.classList.add("d-none");


        // Tạo Admin Dropdown Menu
        adminMenu.innerHTML = `

            <div class="dropdown">

                <a class="nav-link text-white dropdown-toggle"
                   href="#"
                   role="button"
                   data-bs-toggle="dropdown">

                    <i class="bi bi-speedometer2"></i>
                    Admin

                </a>

                <ul class="dropdown-menu dropdown-menu-end dropdown-menu-dark">

                    <li>
                        <a class="dropdown-item" href="/admin">
                            <i class="bi bi-speedometer2 me-2"></i>
                            Admin Dashboard
                        </a>
                    </li>

                    <li>
                        <a class="dropdown-item" href="/admin/products">
                            <i class="bi bi-box-seam me-2"></i>
                            Manage Products
                        </a>
                    </li>

                    <li>
                        <a class="dropdown-item" href="/admin/orders">
                            <i class="bi bi-cart-check me-2"></i>
                            Manage Orders
                        </a>
                    </li>

                    <li>
                        <a class="dropdown-item" href="/admin/users">
                            <i class="bi bi-people me-2"></i>
                            Manage Users
                        </a>
                    </li>

                    <li>
                        <hr class="dropdown-divider">
                    </li>

                    <li>
                        <a class="dropdown-item text-center"
                           href="#"
                           id="logoutBtn">

                            Logout
                            <i class="bi bi-box-arrow-right ms-2"></i>

                        </a>
                    </li>

                </ul>

            </div>
        `;
    }


    // ==================================================
    // LOGOUT
    // ==================================================

    // Tìm nút Logout vừa được tạo trong Navbar
    const logoutBtn = document.getElementById("logoutBtn");


    // Nếu tìm thấy nút Logout
    if (logoutBtn) {

        // Đăng ký sự kiện Click
        logoutBtn.addEventListener("click", async function (e) {

            // Ngăn hành động mặc định của thẻ <a>
            e.preventDefault();


            // Gọi API Logout
            await logout();


            // Sau khi Logout thành công chuyển User về trang Home
            window.location.href = "/";
        });
    }

});
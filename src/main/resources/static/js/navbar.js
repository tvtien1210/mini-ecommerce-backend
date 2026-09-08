// ==================================================
// PAGE LOAD
// ==================================================

document.addEventListener("DOMContentLoaded", async function () {

    // Khởi tạo Navbar
    await initNavbar();

    // Bao gom co logout event...
    setupNavbarEvents();

});


// ==================================================
// INITIALIZE NAVBAR
// ==================================================

async function initNavbar() {

    // ==================================================
    // GET ELEMENTS
    // ==================================================

    // Lấy vị trí hiển thị thông tin User trên Navbar
    const userInfo = document.getElementById("userInfo");

    // Lấy vị trí hiển thị Admin / Staff Menu trên Navbar
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

        // Hiển thị Login
        renderLogin(userInfo);

        // Không cần xử lý tiếp
        return;
    }


    // ==================================================
    // UPDATE CART BADGE
    // ==================================================

    // User đã đăng nhập
    // → lấy Cart và cập nhật Cart Badge
    await updateNavbarCartBadge();


    // ==================================================
    // GET USER INFORMATION
    // ==================================================

    // Lấy username từ CurrentUserDTO
    const username = user.username;

    // Lấy danh sách Role
    const roles = user.roles || [];


    // ==================================================
    // CHECK ROLES
    // ==================================================

    // Kiểm tra User có phải Customer hay không
    const isCustomer = roles.includes("ROLE_CUSTOMER");

    // Kiểm tra User có phải Staff hay không
    const isStaff = roles.includes("ROLE_STAFF");

    // Kiểm tra User có phải Admin hay không
    const isAdmin = roles.includes("ROLE_ADMIN");


    // ==================================================
    // RENDER USER MENU
    // ==================================================

    /*
     * Ưu tiên:
     *
     * ADMIN
     *   ↓
     * STAFF
     *   ↓
     * CUSTOMER
     */

    if (isAdmin) {

        // User là Admin
        renderAdmin(userInfo, adminMenu, username);

    } else if (isStaff) {

        // User là Staff
        renderStaff(userInfo, adminMenu, username);

    } else if (isCustomer) {

        // User là Customer
        renderCustomer(userInfo,username);

    }

}


// ==================================================
// RENDER LOGIN
// ==================================================

function renderLogin(userInfo) {

    // Hiển thị Login
    userInfo.innerHTML = `
        <a class="nav-link text-white" href="/login">

            <i class="bi bi-person-circle"></i>

            Login

        </a>
    `;
}


// ==================================================
// UPDATE CART BADGE
// ==================================================

async function updateNavbarCartBadge() {

    try {

        // ==================================================
        // CALL CART API
        // ==================================================

        // Gọi API lấy Cart hiện tại
        const cartResponse = await apiFetch("/api/cart/my");


        // ==================================================
        // CHECK RESPONSE
        // ==================================================

        // Nếu API thất bại
        if (!cartResponse.ok) {

            // Throw Error
            // → chuyển xuống catch
            throw new Error("Cart API failed");
        }


        // ==================================================
        // PARSE JSON
        // ==================================================

        // Chuyển response thành CartDTO
        const cart = await cartResponse.json();


        // ==================================================
        // UPDATE CART BADGE
        // ==================================================

        // Cập nhật Cart Badge
        updateCartBadge(cart);


    } catch (error) {

        // ==================================================
        // HANDLE ERROR
        // ==================================================

        /*
         * Nếu Cart API lỗi:
         *
         * Không làm Navbar bị crash
         *
         * Chỉ log lỗi ra Console
         */

        console.error(
            "Failed to load cart:",
            error
        );
    }
}


// ==================================================
// RENDER CUSTOMER
// ==================================================

function renderCustomer(userInfo,username) {

    // ==================================================
    // SHOW USER INFO
    // ==================================================

    // Hiển thị User Info
    userInfo.classList.remove("d-none");


    // ==================================================
    // RENDER CUSTOMER MENU
    // ==================================================

    userInfo.innerHTML = `

        <div class="dropdown">

            <a class="nav-link text-white dropdown-toggle"
               href="#"
               role="button"
               data-bs-toggle="dropdown">

                <i class="bi bi-person-circle me-1"></i>

                ${username}

            </a>


            <ul class="dropdown-menu dropdown-menu-end dropdown-menu-dark">

                <li>

                    <a class="dropdown-item text-start"
                       href="#">

                        <i class="bi bi-person me-2"></i>

                        Profile

                    </a>

                </li>


                <li>

                    <a class="dropdown-item text-start"
                       href="/myorders">

                        <i class="bi bi-bag-check me-2"></i>

                        My Orders

                    </a>

                </li>


                <li>

                    <hr class="dropdown-divider">

                </li>


                <li>

                    <a class="dropdown-item text-center logout-btn"
                       href="#">

                        Logout

                        <i class="bi bi-box-arrow-right ms-2"></i>

                    </a>

                </li>

            </ul>

        </div>
    `;
}


// ==================================================
// RENDER STAFF
// ==================================================

function renderStaff(userInfo, adminMenu, username) {

    // ==================================================
    // SHOW STAFF MENU
    // ==================================================

    adminMenu.classList.remove("d-none");


    // ==================================================
    // HIDE USER INFO
    // ==================================================

    userInfo.classList.add("d-none");


    // ==================================================
    // RENDER STAFF DROPDOWN
    // ==================================================

    adminMenu.innerHTML = `

        <div class="dropdown">

            <a class="nav-link text-white dropdown-toggle"
               href="#"
               role="button"
               data-bs-toggle="dropdown">

                <i class="bi bi-person-badge me-1"></i>

                ${username}

            </a>


            <ul class="dropdown-menu dropdown-menu-end dropdown-menu-dark">

                <li>

                    <a class="dropdown-item"
                       href="/#">

                        <i class="bi bi-speedometer2 me-2"></i>

                        Staff Dashboard

                    </a>

                </li>


                <li>

                    <a class="dropdown-item"
                       href="#">

                        <i class="bi bi-cart-check me-2"></i>

                        Manage Orders

                    </a>

                </li>


                <li>

                    <hr class="dropdown-divider">

                </li>


                <li>

                    <a class="dropdown-item text-center logout-btn"
                       href="#">

                        Logout

                        <i class="bi bi-box-arrow-right ms-2"></i>

                    </a>

                </li>

            </ul>

        </div>
    `;
}


// ==================================================
// RENDER ADMIN
// ==================================================

function renderAdmin(userInfo, adminMenu,username) {

    // ==================================================
    // SHOW ADMIN MENU
    // ==================================================

    adminMenu.classList.remove("d-none");


    // ==================================================
    // HIDE USER INFO
    // ==================================================

    userInfo.classList.add("d-none");


    // ==================================================
    // RENDER ADMIN DROPDOWN
    // ==================================================

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

                    <a class="dropdown-item"
                       href="#">

                        <i class="bi bi-speedometer2 me-2"></i>

                        Admin Dashboard

                    </a>

                </li>


                <li>

                    <a class="dropdown-item"
                       href="/admin/products">

                        <i class="bi bi-box-seam me-2"></i>

                        Manage Products

                    </a>

                </li>


                <li>

                    <a class="dropdown-item"
                       href="#">

                        <i class="bi bi-cart-check me-2"></i>

                        Manage Orders

                    </a>

                </li>


                <li>

                    <a class="dropdown-item"
                       href="/#">

                        <i class="bi bi-people me-2"></i>

                        Manage Users

                    </a>

                </li>


                <li>

                    <hr class="dropdown-divider">

                </li>


                <li>

                    <a class="dropdown-item text-center logout-btn"
                       href="#">

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

function setupNavbarEvents(){
// ==================================================
    // GET LOGOUT BUTTON
    // ==================================================

    // Tìm Logout Button vừa được render
    const logoutBtn = document.querySelector(".logout-btn");


    // ==================================================
    // CHECK BUTTON
    // ==================================================

    if (!logoutBtn) {
        return;
    }


    // ==================================================
    // CLICK EVENT
    // ==================================================

    logoutBtn.addEventListener("click", async function (e) {

        // Ngăn hành động mặc định của <a>
        e.preventDefault();


        try {

            // ==================================================
            // CALL LOGOUT API
            // ==================================================

            await logout();


            // ==================================================
            // REDIRECT
            // ==================================================

            // Sau khi Logout thành công
            // chuyển User về Home
            window.location.href = "/";


        } catch (error) {

            // ==================================================
            // HANDLE LOGOUT ERROR
            // ==================================================

            console.error(
                "Logout failed:",
                error
            );
        }
    });
}

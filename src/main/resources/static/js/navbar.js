import { getCurrentUser, logout } from "./auth.js";


document.addEventListener("DOMContentLoaded", async function () {

    const userInfo = document.getElementById("userInfo");
    const adminMenu = document.getElementById("adminMenu");

            // Đã đăng nhập
            const user = await getCurrentUser();

            // Chưa đăng nhập
            if (!user) {

                userInfo.innerHTML = `
                    <a class="nav-link text-white" href="/login">
                        <i class="bi bi-person-circle"></i>
                        Login
                    </a>
                `;

                return;
            }

            const username = user.username;
            const roles = user.roles || [];

            const isAdmin = roles.includes("ROLE_ADMIN");
            const isCustomer = roles.includes("ROLE_CUSTOMER");

            // CUSTOMER

            if (isCustomer) {

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

            // =========================
            // ADMIN
            // =========================

            if (isAdmin) {

                adminMenu.classList.remove("d-none");

                userInfo.classList.add("d-none");

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

            // LOGOUT

            const logoutBtn = document.getElementById("logoutBtn");

            if (logoutBtn) {

                logoutBtn.addEventListener("click", async function (e) {

                    e.preventDefault();

                    await logout();

                });
            }

});
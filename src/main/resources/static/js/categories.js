import { apiFetch } from "./api.js";


// ========================================
// LOAD CATEGORIES
// ========================================

async function loadCategories() {

    // Lấy các element cần sử dụng
    const categoryLoading = document.getElementById("categoryLoading");
    const categoryList = document.getElementById("categoryList");
    const categoryEmpty = document.getElementById("categoryEmpty");
    const categoryError = document.getElementById("categoryError");
    const categoryErrorMessage =
        document.getElementById("categoryErrorMessage");

    try {

        // Hiển thị Loading
        categoryLoading.classList.remove("d-none");

        // Ẩn các trạng thái khác
        categoryList.classList.add("d-none");
        categoryEmpty.classList.add("d-none");
        categoryError.classList.add("d-none");


        // Gọi API lấy categories
        const response = await apiFetch("/api/categories");


        // Kiểm tra HTTP response
        if (!response.ok) {

            console.error(
                "Failed to load categories:",
                response.status,
                response.statusText
            );

            categoryErrorMessage.textContent =
                `Failed to load categories (${response.status}).`;

            categoryError.classList.remove("d-none");

            return;
        }


        // Chuyển Response Body thành JavaScript object/array
        const categories = await response.json();


        // Kiểm tra không có category
        if (!categories || categories.length === 0) {

            categoryEmpty.classList.remove("d-none");

            return;
        }


        // Render categories
        renderCategory(categories);


    } catch (error) {

        console.error(
            "Error loading categories:",
            error
        );

        categoryErrorMessage.textContent =
            "Unable to connect to the server.";

        categoryError.classList.remove("d-none");

    } finally {

        // Luôn tắt Loading
        categoryLoading.classList.add("d-none");

    }
}


// ========================================
// RENDER CATEGORIES
// ========================================

function renderCategory(categories) {

    // Lấy container chứa category cards
    const categoryList =
        document.getElementById("categoryList");


    // Xóa dữ liệu cũ nếu có
    categoryList.innerHTML = "";


    // Tạo card cho từng category
    categories.forEach(category => {

        // Tạo column
        const column =
            document.createElement("div");

        column.className =
            "col-12 col-md-6 col-lg-4";


        // Tạo category card
        column.innerHTML = `

            <div class="category-card text-center">

                <!-- Category icon -->
                <div class="category-icon">

                    <i class="bi bi-grid"></i>

                </div>


                <!-- Category name -->
                <h2 class="category-name mb-3">

                    ${category.name}

                </h2>


                <!-- Category description -->
                <p class="category-description">

                    ${category.description ?? ""}

                </p>


                <!-- View products -->
                <a
                    class="btn btn-success view-products-btn"
                    href="/products?categoryId=${category.id}">

                    <i class="bi bi-arrow-right"></i>

                    View Products

                </a>

            </div>

        `;


        // Thêm column vào categoryList
        categoryList.appendChild(column);

    });


    // Hiển thị category list
    categoryList.classList.remove("d-none");
}


// ========================================
// RETRY
// ========================================

const categoryRetryBtn =
    document.getElementById("categoryRetryBtn");


if (categoryRetryBtn) {

    categoryRetryBtn.addEventListener(
        "click",
        loadCategories
    );

}


// ========================================
// INITIAL LOAD
// ========================================

document.addEventListener(
    "DOMContentLoaded",
    loadCategories
);

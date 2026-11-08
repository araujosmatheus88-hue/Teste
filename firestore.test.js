const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
let testEnv;

before(async () => {
  const rules = fs.readFileSync("firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host: "127.0.0.1",
      port: 8085,
      rules: rules
    }
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read dramas", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("dramas").get());
});

test("Authenticated regular user can read categories", async () => {
  const userDb = testEnv.authenticatedContext("user_123", { email: "user@example.com" }).firestore();
  await assertSucceeds(userDb.collection("categories").get());
});

test("Regular user cannot create a drama", async () => {
  const userDb = testEnv.authenticatedContext("user_123", { email: "user@example.com" }).firestore();
  await assertFails(userDb.collection("dramas").doc("drama_test").set({
    title: "Hack",
    description: "test",
    coverVerticalUrl: "https://test.com/img.jpg",
    categoryId: "cat_1",
    categoryName: "Romance",
    isPublished: true
  }));
});

test("Admin user (araujosmatheus88@gmail.com) can create a drama", async () => {
  const adminDb = testEnv.authenticatedContext("admin_123", {
    email: "araujosmatheus88@gmail.com",
    email_verified: true
  }).firestore();

  await assertSucceeds(adminDb.collection("dramas").doc("drama_test").set({
    title: "O Segredo do Bilionário",
    description: "Uma história de romance e vingança.",
    coverVerticalUrl: "https://test.com/cover.jpg",
    categoryId: "cat_1",
    categoryName: "Romance",
    isPublished: true
  }));
});

test("User can create and read their own favorites", async () => {
  const userDb = testEnv.authenticatedContext("user_abc", { email: "abc@test.com" }).firestore();
  await assertSucceeds(userDb.collection("favorites").doc("fav_1").set({
    userId: "user_abc",
    dramaId: "drama_01"
  }));
  await assertSucceeds(userDb.collection("favorites").doc("fav_1").get());
});

test("User cannot read another user's favorites", async () => {
  // First setup document as user_abc
  const userDb1 = testEnv.authenticatedContext("user_abc", { email: "abc@test.com" }).firestore();
  await userDb1.collection("favorites").doc("fav_1").set({
    userId: "user_abc",
    dramaId: "drama_01"
  });

  // Try reading as user_xyz
  const userDb2 = testEnv.authenticatedContext("user_xyz", { email: "xyz@test.com" }).firestore();
  await assertFails(userDb2.collection("favorites").doc("fav_1").get());
});

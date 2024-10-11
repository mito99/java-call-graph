describe("My Test Suite", () => {
  let sharedValue: string;

  beforeAll(() => {
    // sharedValueを初期化
    sharedValue = "initialized value";
  });

  test("Test 1", () => {
    // beforeAllで初期化されたsharedValueを参照
    expect(sharedValue).toBe("initialized value");
  });

  test("Test 2", () => {
    // 他のテストでもsharedValueを利用可能
    expect(sharedValue).toBe("initialized value");
  });
});

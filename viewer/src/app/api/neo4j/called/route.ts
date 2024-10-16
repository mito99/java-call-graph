import { getCallingMethodsByDigest, getSession } from "@/lib/neo4j";
import { NextResponse } from "next/server";

export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const methodDigest = searchParams.get("methodDigest") ?? "";
    const hopCount = parseInt(searchParams.get("hopCount") ?? "5");

    const session = getSession();
    const records = await getCallingMethodsByDigest(session, {
      methodDigest,
      hopCount,
    });
    await session.close();

    return NextResponse.json(records);
  } catch (error) {
    console.error("Error fetching data from Neo4j:", error);
    return NextResponse.json(
      { error: "Internal Server Error" },
      { status: 500 }
    );
  }
}

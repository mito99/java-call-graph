import { JavaCallHierarchyComponent } from "@/components/java-call-hierarchy";
import { Loader } from "@/components/ui/loader";
import { Suspense } from "react";

export default function Home() {
  return (
    <div>
      <Suspense 
        fallback={
          <div className="flex justify-center items-center min-h-screen">
            <Loader />
          </div>
        }
      >
        <JavaCallHierarchyComponent />
      </Suspense>
    </div>
  );
}
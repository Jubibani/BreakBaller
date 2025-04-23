using UnityEngine;

public class BouncyBall : MonoBehaviour
{
    //bouncy ball's rigid body
    private Rigidbody2D rigidBody2D;
    
    //line Y axis  indicator where if the ball passes this value, the game resets.
    public float minimumY = -5.5f;
    void Start()
    {
        //reference the rigid body of the bouncy ball from the added components
        rigidBody2D = GetComponent<Rigidbody2D>();

    }


    void Update()
    {
        //if the ball has its transform position value is lesser than the minimumY
        if (transform.position.y < minimumY)
        {
            //then we spawn the ball's position back to the viewport where it is visible
            transform.position = Vector3.zero;
            rigidBody2D.linearVelocity = Vector3.zero;
        }
      
    }
}
